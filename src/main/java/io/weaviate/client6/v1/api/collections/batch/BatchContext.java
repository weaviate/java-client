package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

import javax.annotation.concurrent.GuardedBy;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.batch.Event.RpcError;
import io.weaviate.client6.v1.api.collections.data.BatchReference;
import io.weaviate.client6.v1.api.collections.data.InsertManyRequest;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

/**
 * BatchContext stores the state of an active batch process
 * and controls its lifecycle.
 *
 * <h2>State</h2>
 *
 * <h2>Lifecycle</h2>
 *
 * <h2>Cancellation policy</h2>
 *
 * @param <PropertiesT> the shape of properties for inserted objects.
 */
public final class BatchContext<PropertiesT> implements Closeable {
  private final int DEFAULT_BATCH_SIZE = 1000;
  private final int DEFAULT_QUEUE_SIZE = 100;

  private final CollectionDescriptor<PropertiesT> collectionDescriptor;
  private final CollectionHandleDefaults collectionHandleDefaults;

  /** Stream factory creates new streams. */
  private final StreamFactory<Message, Event> streamFactory;

  /**
   * Queue publishes insert tasks from the main thread to the "sender".
   * It has a maximum capacity of {@link #DEFAULT_QUEUE_SIZE}.
   *
   * Send {@link TaskHandle#POISON} to gracefully shutdown the "sender"
   * thread. The same queue may be re-used with a different "sender",
   * e.g. after {@link #reconnect}, but only when the new thread is known
   * to have started. Otherwise the thread trying to put an item on
   * the queue will block indefinitely.
   */
  private final BlockingQueue<TaskHandle> queue;

  /**
   * Work-in-progress items.
   *
   * An item is added to the {@link #wip} map after the Sender successfully
   * adds it to the {@link #batch} and is removed once the server reports
   * back the result (whether success of failure).
   */
  private final ConcurrentMap<String, TaskHandle> wip = new ConcurrentHashMap<>();

  /**
   * Current batch.
   *
   * <p>
   * An item is added to the {@link #batch} after the Sender pulls it
   * from the queue and remains there until it's Ack'ed.
   */
  private final Batch batch;

  /**
   * State encapsulates state-dependent behavior of the {@link BatchContext}.
   * Before reading {@link #state}, a thread MUST acquire {@link #lock}.
   */
  @GuardedBy("lock")
  private State state;
  /** lock synchronizes access to {@link #state}. */
  private final Lock lock = new ReentrantLock();
  /** stateChanged notifies threads about a state transition. */
  private final Condition stateChanged = lock.newCondition();

  /**
   * Internal execution service. It's lifecycle is bound to that of the
   * BatchContext: it's started when the context is initialized
   * and shutdown on {@link #close}.
   *
   * <p>
   * In the event of abrupt stream termination ({@link Recv#onError} is called),
   * the "recv" thread MAY shutdown this service in order to interrupt the "send"
   * thread; the latter may be blocked on {@link Send#awaitCanSend} or
   * {@link Send#awaitCanPrepareNext}.
   */
  private final ExecutorService sendExec = Executors.newSingleThreadExecutor();

  /**
   * Scheduled thread pool for OOM timer.
   *
   * @see Oom
   */
  private final ScheduledExecutorService scheduledExec = Executors.newScheduledThreadPool(1);

  /** Service executor for polling {@link #workers} status before closing. */
  private final ExecutorService closeExec = Executors.newSingleThreadExecutor();

  /**
   * Client-side part of the current stream, created on {@link #start}.
   * Other threads MAY use stream but MUST NOT update this field on their own.
   */
  private volatile StreamObserver<Message> messages;

  /**
   * Server-side part of the current stream, created on {@link #start}.
   * Other threads MAY use stream but MUST NOT update this field on their own.
   */
  private volatile StreamObserver<Event> events;

  /**
   * Latch reaches zero once both "send" (client side) and "recv" (server side)
   * parts of the stream have closed. After a {@link reconnect}, the latch is
   * reset.
   */
  private volatile CountDownLatch workers;

  /** done completes the stream. */
  private final CompletableFuture<Void> closed = new CompletableFuture<>();

  /** Thread which created the BatchContext. */
  private final Thread parent = Thread.currentThread();

  BatchContext(
      StreamFactory<Message, Event> streamFactory,
      int maxSizeBytes,
      CollectionDescriptor<PropertiesT> collectionDescriptor,
      CollectionHandleDefaults collectionHandleDefaults) {
    this.streamFactory = requireNonNull(streamFactory, "streamFactory is null");
    this.collectionDescriptor = requireNonNull(collectionDescriptor, "collectionDescriptor is null");
    this.collectionHandleDefaults = requireNonNull(collectionHandleDefaults, "collectionHandleDefaults is null");

    this.queue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_SIZE);
    this.batch = new Batch(DEFAULT_BATCH_SIZE, maxSizeBytes);
  }

  /** Add {@link WeaviateObject} to the batch. */
  public TaskHandle add(WeaviateObject<PropertiesT> object) throws InterruptedException {
    TaskHandle handle = new TaskHandle(
        object,
        InsertManyRequest.buildObject(object, collectionDescriptor, collectionHandleDefaults));
    return add(handle);
  }

  /** Add {@link BatchReference} to the batch. */
  public TaskHandle add(BatchReference reference) throws InterruptedException {
    TaskHandle handle = new TaskHandle(
        reference,
        InsertManyRequest.buildReference(reference, collectionHandleDefaults.tenant()));
    return add(handle);
  }

  void start() {
    workers = new CountDownLatch(2);

    Recv recv = new Recv();
    messages = streamFactory.createStream(recv);
    events = recv;

    Send send = new Send();
    sendExec.execute(send);
  }

  void reconnect() throws InterruptedException {
    workers.await();
    start();
  }

  /**
   * Retry a task.
   *
   * BatchContext does not impose any limit on the number of times a task can
   * be retried -- it is up to the user to implement an appropriate retry policy.
   *
   * @see TaskHandle#timesRetried
   */
  public TaskHandle retry(TaskHandle taskHandle) throws InterruptedException {
    return add(taskHandle.retry());
  }

  /**
   * Interrupt all subprocesses, notify the server, de-allocate resources,
   * and abort the stream.
   *
   * @apiNote This is not a normal shutdown process. It is an abrupt termination
   *          triggered by an exception.
   */
  private void abort(Throwable t) {
    messages.onError(Status.INTERNAL.withCause(t).asRuntimeException());
    closed.completeExceptionally(t);
    parent.interrupt();
    sendExec.shutdown();
  }

  @Override
  public void close() throws IOException {
    closeExec.execute(() -> {
      try {
        queue.put(TaskHandle.POISON);
        workers.await();
        closed.complete(null);
      } catch (Exception e) {
        closed.completeExceptionally(e);
      }
    });

    try {
      closed.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw new IOException(e.getCause());
    } finally {
      shutdownExecutors();
    }
  }

  private void shutdownExecutors() {
    BiConsumer<String, List<Runnable>> assertEmpty = (name, pending) -> {
      assert pending.isEmpty() : "'%s' service had %d tasks awaiting execution"
          .formatted(pending.size(), name);
    };

    List<Runnable> pending;

    pending = sendExec.shutdownNow();
    assertEmpty.accept("send", pending);

    pending = scheduledExec.shutdownNow();
    assertEmpty.accept("oom", pending);

    pending = closeExec.shutdownNow();
    assertEmpty.accept("close", pending);
  }

  /** Set the new state and notify awaiting threads. */
  void setState(State nextState) {
    requireNonNull(nextState, "nextState is null");

    lock.lock();
    try {
      State prev = state;
      state = nextState;
      state.onEnter(prev);
      stateChanged.signal();
    } finally {
      lock.unlock();
    }
  }

  /** onEvent delegates event handling to {@link #state} */
  void onEvent(Event event) throws InterruptedException {
    lock.lock();
    try {
      state.onEvent(event);
    } finally {
      lock.unlock();
    }
  }

  private TaskHandle add(final TaskHandle taskHandle) throws InterruptedException {
    if (closed.isDone()) {
      throw new IllegalStateException("BatchContext is closed");
    }

    TaskHandle existing = wip.get(taskHandle.id());
    if (existing != null) {
      throw new DuplicateTaskException(taskHandle, existing);
    }

    existing = wip.put(taskHandle.id(), taskHandle);
    assert existing == null : "duplicate tasks in progress, id=" + existing.id();

    queue.put(taskHandle);
    return taskHandle;
  }

  private final class Send implements Runnable {

    @Override
    public void run() {
      try {
        trySend();
      } catch (Exception e) {
        messages.onError(e);
      } finally {
        workers.countDown();
      }
    }

    /**
     * trySend consumes {@link #queue} tasks and sends them in batches until it
     * encounters a {@link TaskHandle#POISON}.
     *
     * <p>
     * If the method returns normally, it means the queue's been drained.
     */
    private void trySend() throws DataTooBigException {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          if (batch.isFull()) {
            send();
          }

          TaskHandle task = queue.take();

          if (task == TaskHandle.POISON) {
            drain();
            return;
          }

          Data data = task.data();
          batch.add(data);

          TaskHandle existing = wip.put(task.id(), task);
          assert existing == null : "duplicate tasks in progress, id=" + existing.id();
        }
      } catch (InterruptedException ignored) {
        messages.onNext(Message.stop());
        messages.onCompleted();
        Thread.currentThread().interrupt();
      }
    }

    /**
     * Send the current portion of batch items. After this method returns, the batch
     * is guaranteed to have space for at least one the next item (not full).
     */
    private void send() throws InterruptedException {
      // Continue flushing until we get the batch to not a "not full" state.
      // This is to account for the backlog, which might re-fill the batch
      // after .clear().
      while (batch.isFull()) {
        flush();
      }
      assert !batch.isFull() : "batch is full after send";
    }

    /**
     * Send all remaining items in the batch. After this method returns, the batch
     * is guaranteed to be empty.
     */
    private void drain() throws InterruptedException {
      // To correctly drain the batch, we flush repeatedly
      // until the batch becomes empty, as clearing a batch
      // after an ACK might re-populate it from its internal backlog.
      while (!batch.isEmpty()) {
        flush();
      }
      assert batch.isEmpty() : "batch not empty after drain";
    }

    private void flush() throws InterruptedException {
      awaitCanSend();
      messages.onNext(batch.prepare());
      setState(IN_FLIGHT);

      // When we get into OOM / ServerShuttingDown state, then we can be certain that
      // there isn't any reason to keep waiting for the ACKs. However, we should not
      // exit without either taking a poison pill from the queue,
      // or being interrupted, as this risks blocking the producer (main) thread.
      awaitCanPrepareNext();
    }

    /** Block until the current state allows {@link State#canSend}. */
    private void awaitCanSend() throws InterruptedException {
      lock.lock();
      try {
        while (!state.canSend()) {
          stateChanged.await();
        }
      } finally {
        lock.unlock();
      }
    }

    /**
     * Block until the current state allows {@link State#canPrepareNext}.
     *
     * <p>
     * Depending on the BatchContext lifecycle, the semantics of
     * "await can prepare next" can be one of "message is ACK'ed"
     * "the stream has started", or, more generally,
     * "it is safe to take a next item from the queue and add it to the batch".
     */
    private void awaitCanPrepareNext() throws InterruptedException {
      lock.lock();
      try {
        while (!state.canPrepareNext()) {
          stateChanged.await();
        }
      } finally {
        lock.unlock();
      }
    }
  }

  private final class Recv implements StreamObserver<Event> {

    @Override
    public void onNext(Event event) {
      try {
        onEvent(event);
      } catch (InterruptedException e) {
        // Recv is running on a thread from gRPC's internal thread pool,
        // so, while onEvent allows InterruptedException to stay responsive,
        // in practice this thread will only be interrupted by the thread pool,
        // which already knows it's being shut down.
      }
    }

    /**
     * EOF for the server-side stream.
     * By the time this is called, the client-side of the stream had been closed
     * and the "send" thread has either exited or is on its way there.
     */
    @Override
    public void onCompleted() {
      workers.countDown();

      // boolean stillStuffToDo = true;
      // if (stillStuffToDo) {
      // reconnect();
      // }
    }

    /** An exception occurred either on our end or in the channel internals. */
    @Override
    public void onError(Throwable t) {
      try {
        onEvent(Event.RpcError.fromThrowable(t));
      } catch (InterruptedException ignored) {
        // Recv is running on a thread from gRPC's internal thread pool,
        // so, while onEvent allows InterruptedException to stay responsive,
        // in practice this thread will only be interrupted by the thread pool,
        // which already knows it's being shut down.
      } finally {
        workers.countDown();
      }
    }
  }

  final State CLOSED = new BaseState("CLOSED");
  final State AWAIT_STARTED = new BaseState("AWAIT_STARTED", BaseState.Action.PREPARE_NEXT) {
    @Override
    public void onEvent(Event event) throws InterruptedException {
      if (requireNonNull(event, "event is null") == Event.STARTED) {
        setState(ACTIVE);
        return;
      }
      super.onEvent(event);
    }
  };
  final State ACTIVE = new BaseState("ACTIVE", BaseState.Action.PREPARE_NEXT, BaseState.Action.SEND);
  final State IN_FLIGHT = new BaseState("IN_FLIGHT") {
    @Override
    public void onEvent(Event event) throws InterruptedException {
      requireNonNull(event, "event is null");

      if (event instanceof Event.Acks acks) {
        Collection<String> remaining = batch.clear();
        if (!remaining.isEmpty()) {
          throw ProtocolViolationException.incompleteAcks(List.copyOf(remaining));
        }
        acks.acked().forEach(id -> {
          TaskHandle task = wip.get(id);
          if (task != null) {
            task.setAcked();
          }
        });
        setState(ACTIVE);
      } else if (event == Event.OOM) {
        int delaySeconds = 300;
        setState(new Oom(delaySeconds));
      } else {
        super.onEvent(event);
      }
    }
  };

  private class BaseState implements State {
    private final String name;
    private final EnumSet<Action> permitted;

    enum Action {
      PREPARE_NEXT, SEND;
    }

    protected BaseState(String name, Action... actions) {
      this.name = name;
      this.permitted = EnumSet.copyOf(Arrays.asList(requireNonNull(actions, "actions is null")));
    }

    @Override
    public void onEnter(State prev) {
    }

    @Override
    public boolean canSend() {
      return permitted.contains(Action.SEND);
    }

    @Override
    public boolean canPrepareNext() {
      return permitted.contains(Action.PREPARE_NEXT);
    }

    @Override
    public void onEvent(Event event) throws InterruptedException {
      requireNonNull(event, "event is null");

      if (event instanceof Event.Results results) {
        results.successful().forEach(id -> wip.get(id).setSuccess());
        results.errors().forEach((id, error) -> wip.get(id).setError(error));
      } else if (event instanceof Event.Backoff backoff) {
        batch.setMaxSize(backoff.maxSize());
      } else if (event == Event.SHUTTING_DOWN) {
        setState(new ServerShuttingDown(this));
      } else if (event instanceof Event.RpcError error) {
        setState(new StreamAborted(error.exception()));
      } else {
        throw ProtocolViolationException.illegalStateTransition(this, event);
      }
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Oom waits for {@link Event#SHUTTING_DOWN} up to a specified amount of time,
   * after which it will force stream termiation by imitating server shutdown.
   */
  private final class Oom extends BaseState {
    private final long delaySeconds;
    private ScheduledFuture<?> shutdown;

    private Oom(long delaySeconds) {
      super("OOM");
      this.delaySeconds = delaySeconds;
    }

    @Override
    public void onEnter(State prev) {
      shutdown = scheduledExec.schedule(this::initiateShutdown, delaySeconds, TimeUnit.SECONDS);
    }

    /** Imitate server shutdown sequence. */
    private void initiateShutdown() {
      if (Thread.currentThread().isInterrupted()) {
        return;
      }
      events.onNext(Event.SHUTTING_DOWN);
      events.onNext(Event.SHUTDOWN);
    }

    @Override
    public void onEvent(Event event) throws InterruptedException {
      requireNonNull(event, "event");
      if (event == Event.SHUTTING_DOWN || event instanceof RpcError) {
        shutdown.cancel(true);
        try {
          shutdown.get();
        } catch (CancellationException ignored) {
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
      super.onEvent(event);
    }
  }

  /**
   * ServerShuttingDown allows preparing the next batch
   * unless the server's OOM'ed on the previous one.
   * Once set, the state will shutdown {@link BatchContext#sendExec}
   * to instruct the "send" thread to close our part of the stream.
   */
  private final class ServerShuttingDown extends BaseState {
    private final boolean canPrepareNext;

    private ServerShuttingDown(State previous) {
      super("SERVER_SHUTTING_DOWN");
      this.canPrepareNext = requireNonNull(previous, "previous is null").getClass() != Oom.class;
    }

    @Override
    public boolean canPrepareNext() {
      return canPrepareNext;
    }

    @Override
    public boolean canSend() {
      return false;
    }

    @Override
    public void onEnter(State prev) {
      sendExec.shutdown();
    }

    // TODO(dyma): if we agree to retire Shutdown, then ServerShuttingDown
    // should not override onEvent and let it fallthough to
    // ProtocolViolationException on any event.
    @Override
    public void onEvent(Event event) throws InterruptedException {
      if (requireNonNull(event, "event is null") == Event.SHUTDOWN) {
        return;
      }
      super.onEvent(event);
    }
  }

  /**
   * StreamAborted means the RPC is "dead": the {@link messages} stream is closed
   * and using it will result in an {@link IllegalStateException}.
   */
  private final class StreamAborted extends BaseState {
    private final Throwable t;

    protected StreamAborted(Throwable t) {
      super("STREAM_ABORTED");
      this.t = t;
    }

    @Override
    public void onEnter(State prev) {
      abort(t);
    }

    @Override
    public void onEvent(Event event) {
      // StreamAborted cannot transition into another state. It is terminal --
      // BatchContext MUST terminate its subprocesses and close exceptionally.
    }
  }
}
