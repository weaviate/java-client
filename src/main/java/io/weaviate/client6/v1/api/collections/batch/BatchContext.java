package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
import java.util.concurrent.Future;
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
import io.weaviate.client6.v1.api.collections.batch.Event.ClientError;
import io.weaviate.client6.v1.api.collections.batch.Event.StreamHangup;
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
  private final int MAX_RECONNECT_RETRIES = 5;

  private final CollectionDescriptor<PropertiesT> collectionDescriptor;
  private final CollectionHandleDefaults collectionHandleDefaults;

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
   * Scheduled thread pool for delayed tasks.
   *
   * @see Oom
   * @see Reconnecting
   */
  private final ScheduledExecutorService scheduledExec = Executors.newScheduledThreadPool(1);

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
   * Client-side part of the current stream, created on {@link #start}.
   * Other threads MAY use stream but MUST NOT update this field on their own.
   */
  private volatile StreamObserver<Message> messages;

  /** Handle for the "send" thread. Use {@link Future#cancel} to interrupt it. */
  private volatile Future<?> send;

  /**
   * Latch reaches zero once both "send" (client side) and "recv" (server side)
   * parts of the stream have closed. After a {@link reconnect}, the latch is
   * reset.
   */
  private volatile CountDownLatch workers;

  /** Lightway check to ensure users cannot send on a closed context. */
  private volatile boolean closed;

  /** Closing state. */
  private volatile Closing closing;

  /**
   * setClosing trasitions BatchContext to {@link Closing} state exactly once.
   * Once this method returns, the caller can call {@code closing.await()}.
   */
  void setClosing(Exception ex) {
    if (closing == null) {
      synchronized (Closing.class) {
        if (closing == null) {
          closing = new Closing(ex);
          setState(closing);
        }
      }
    }
  }

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
    setState(CLOSED);

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

    messages = streamFactory.createStream(new Recv());
    send = sendExec.submit(new Send());

    messages.onNext(Message.start(collectionHandleDefaults.consistencyLevel()));
    setState(AWAIT_STARTED);
  }

  /**
   * Reconnect waits for "send" and "recv" streams to exit
   * and restarts the process with a new stream.
   */
  void reconnect() throws InterruptedException, ExecutionException {
    workers.await();
    send.get();
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
   * Close attempts to drain the queue and send all remaining items.
   * Calling any of BatchContext's public methods afterwards will
   * result in an {@link IllegalStateException}.
   *
   * @throws IOException Propagates an exception
   *                     if one has occurred in the meantime.
   */
  @Override
  public void close() throws IOException {
    setClosing(null);
    assert closing != null : "closing state not set";

    try {
      closing.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw new IOException(e.getCause());
    } finally {
      shutdownExecutors();
      setState(CLOSED);
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

    pending = closing.shutdownNow();
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

  /**
   * onEvent delegates event handling to {@link #state}.
   *
   * <p>
   * Be mindful that most of the time this callback will run in a hot path
   * on a gRPC thread. {@link State} implementations SHOULD offload any
   * blocking operations to one of the provided executors.
   *
   * @see #scheduledExec
   */
  private void onEvent(Event event) {
    lock.lock();
    try {
      state.onEvent(event);
    } finally {
      lock.unlock();
    }
  }

  private TaskHandle add(final TaskHandle taskHandle) throws InterruptedException {
    if (closed) {
      throw new IllegalStateException("BatchContext is closed");
    }

    TaskHandle existing = wip.get(taskHandle.id());
    if (existing != null) {
      throw new DuplicateTaskException(taskHandle, existing);
    }

    queue.put(taskHandle);
    return taskHandle;
  }

  private final class Send implements Runnable {

    @Override
    public void run() {
      try {
        trySend();
      } finally {
        workers.countDown();
      }
    }

    /**
     * trySend consumes {@link #queue} tasks and sends them in batches until it
     * encounters a {@link TaskHandle#POISON} or is otherwise interrupted.
     */
    private void trySend() {
      try {
        awaitCanPrepareNext();

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
        // This thread is only interrupted in the RECONNECTING state, not by
        // the user's code. Allow this method to exit normally to close our
        // end of the stream.
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        onEvent(new Event.ClientError(e));
        return;
      }

      messages.onNext(Message.stop());
      messages.onCompleted();
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
      onEvent(event);
    }

    /**
     * EOF for the server-side stream.
     * By the time this is called, the client-side of the stream had been closed
     * and the "send" thread has either exited or is on its way there.
     */
    @Override
    public void onCompleted() {
      try {
        onEvent(Event.EOF);
      } finally {
        workers.countDown();
      }
    }

    /** An exception occurred either on our end or in the channel internals. */
    @Override
    public void onError(Throwable t) {
      try {
        onEvent(Event.StreamHangup.fromThrowable(t));
      } finally {
        workers.countDown();
      }
    }
  }

  final State AWAIT_STARTED = new BaseState("AWAIT_STARTED", BaseState.Action.PREPARE_NEXT) {
    @Override
    public void onEvent(Event event) {
      if (requireNonNull(event, "event is null") == Event.STARTED) {
        setState(ACTIVE);
      } else {
        super.onEvent(event);
      }
    }
  };
  final State ACTIVE = new BaseState("ACTIVE", BaseState.Action.PREPARE_NEXT, BaseState.Action.SEND);
  final State IN_FLIGHT = new BaseState("IN_FLIGHT") {
    @Override
    public void onEvent(Event event) {
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
      } else if (event instanceof Event.Oom oom) {
        setState(new Oom(oom.delaySeconds()));
      } else {
        super.onEvent(event);
      }
    }
  };

  private class BaseState implements State {
    /** State's display name for logging. */
    private final String name;
    /** Actions permitted in this state. */
    private final EnumSet<Action> permitted;

    enum Action {
      /**
       * Thy system is allowed to accept new items from the user
       * and populate the next batch.
       */
      PREPARE_NEXT,

      /** The system is allowed to send the next batch once it's ready. */
      SEND;
    }

    /**
     * @param name      Display name.
     * @param permitted Actions permitted in this state.
     */
    protected BaseState(String name, Action... permitted) {
      this.name = name;
      this.permitted = EnumSet.copyOf(Arrays.asList(requireNonNull(permitted, "actions is null")));
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

    /**
     * Handle events which may arrive at any moment without violating the protocol.
     *
     * <ul>
     * <li>{@link Event.Results} -- update tasks in {@link #wip} and remove them.
     * <li>{@link Event.Backoff} -- adjust batch size.
     * <li>{@link Event#SHUTTING_DOWN} -- transition into
     * {@link ServerShuttingDown}.
     * <li>{@link Event.StreamHangup -- transition into {@link Reconnecting} state.
     * <li>{@link Event.ClientError -- transition into {@link Closing} state with
     * exception.
     * </ul>
     *
     * @throws ProtocolViolationException If event cannot be handled in this state.
     */
    @Override
    public void onEvent(Event event) {
      requireNonNull(event, "event is null");

      if (event instanceof Event.Results results) {
        onResults(results);
      } else if (event instanceof Event.Backoff backoff) {
        onBackoff(backoff);
      } else if (event == Event.SHUTTING_DOWN) {
        onShuttingDown();
      } else if (event instanceof Event.StreamHangup || event == Event.EOF) {
        onStreamClosed(event);
      } else if (event instanceof Event.ClientError error) {
        onClientError(error);
      } else {
        throw ProtocolViolationException.illegalStateTransition(this, event);
      }
    }

    private final void onResults(Event.Results results) {
      results.successful().forEach(id -> wip.remove(id).setSuccess());
      results.errors().forEach((id, error) -> wip.remove(id).setError(error));
    }

    private final void onBackoff(Event.Backoff backoff) {
      batch.setMaxSize(backoff.maxSize());
    }

    private final void onShuttingDown() {
      setState(new ServerShuttingDown(this));
    }

    private final void onStreamClosed(Event event) {
      if (event instanceof Event.StreamHangup hangup) {
        // TODO(dyma): log error?
      }
      setState(new Reconnecting(MAX_RECONNECT_RETRIES));
    }

    private final void onClientError(Event.ClientError error) {
      setClosing(error.exception());
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
      // We cannot route event handling via normal BatchContext#onEvent, because
      // it delegates to the current state, which is Oom. If Oom#onEvent were to
      // receive an Event.SHUTTING_DOWN, it would cancel this execution of this
      // very sequence. Instead, we delegate to our parent BaseState which normally
      // handles these events.
      if (Thread.currentThread().isInterrupted()) {
        super.onEvent(Event.SHUTTING_DOWN);
      }
      if (Thread.currentThread().isInterrupted()) {
        super.onEvent(Event.EOF);
      }
    }

    @Override
    public void onEvent(Event event) {
      requireNonNull(event, "event");
      if (event == Event.SHUTTING_DOWN ||
          event instanceof StreamHangup ||
          event instanceof ClientError) {
        shutdown.cancel(true);
        try {
          shutdown.get();
        } catch (CancellationException ignored) {
        } catch (InterruptedException ignored) {
          // Recv is running on a thread from gRPC's internal thread pool,
          // so, while onEvent allows InterruptedException to stay responsive,
          // in practice this thread will only be interrupted by the thread pool,
          // which already knows it's being shut down.
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
      send.cancel(true);
    }
  }

  /**
   * Reconnecting state is entererd either by the server finishing a shutdown
   * and closing it's end of the stream or an unexpected stream hangup.
   *
   * @see Recv#onCompleted graceful server shutdown
   * @see Recv#onError stream hangup
   */
  private final class Reconnecting extends BaseState {
    private final int maxRetries;
    private int retries = 0;

    private Reconnecting(int maxRetries) {
      super("RECONNECTING", Action.PREPARE_NEXT);
      this.maxRetries = maxRetries;
    }

    @Override
    public void onEnter(State prev) {
      send.cancel(true);

      if (prev.getClass() != ServerShuttingDown.class) {
        // This is NOT an orderly shutdown, we're reconnecting after a stream hangup.
        // Assume all WIP items have been lost and re-submit everything.
        // All items in the batch are contained in WIP, so it is safe to discard the
        // batch entirely and re-populate from WIP.
        while (!batch.isEmpty()) {
          batch.clear();
        }

        // Unlike during normal operation, we will not stop when batch.isFull().
        // Batch#add guarantees that data will not be discarded in the event of
        // an overflow -- all extra items are placed into the backlog, which is
        // unbounded.
        wip.values().forEach(task -> batch.add(task.data()));
      }

      reconnectNow();
    }

    @Override
    public void onEvent(Event event) {
      assert retries <= maxRetries : "maxRetries exceeded";

      if (event == Event.STARTED) {
        setState(ACTIVE);
      } else if (event instanceof Event.StreamHangup) {
        if (retries == maxRetries) {
          onEvent(new ClientError(new IOException("Server unavailable")));
        } else {
          reconnectAfter(1 * 2 ^ retries);
        }
      }

      assert retries <= maxRetries : "maxRetries exceeded";
    }

    /** Reconnect with no delay. */
    private void reconnectNow() {
      reconnectAfter(0);
    }

    /**
     * Schedule a task to {@link #reconnect} after a delay.
     *
     * @param delaySeconds Delay in seconds.
     *
     * @apiNote The task is scheduled on {@link #scheduledExec} even if
     *          {@code delaySeconds == 0} to avoid blocking gRPC worker thread,
     *          where the {@link BatchContext#onEvent} callback runs.
     */
    private void reconnectAfter(long delaySeconds) {
      retries++;

      scheduledExec.schedule(() -> {
        try {
          reconnect();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          onEvent(new Event.ClientError(e));
        }
      }, delaySeconds, TimeUnit.SECONDS);
    }
  }

  private final class Closing extends BaseState {
    /** Service executor for polling {@link #workers} status before closing. */
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    /** closed completes the stream. */
    private final CompletableFuture<Void> future = new CompletableFuture<>();

    private final Optional<Exception> ex;

    private Closing(Exception ex) {
      super("CLOSING");
      this.ex = Optional.ofNullable(ex);
    }

    @Override
    public void onEnter(State prev) {
      exec.execute(() -> {
        try {
          stopSend();
          workers.await();
          future.complete(null);
        } catch (Exception e) {
          future.completeExceptionally(e);
        }
      });
    }

    @Override
    public void onEvent(Event event) {
      if (event != Event.EOF) {
        super.onEvent(event); // falthrough
      }
    }

    private void stopSend() throws InterruptedException {
      if (ex.isEmpty()) {
        queue.put(TaskHandle.POISON);
      } else {
        messages.onError(Status.INTERNAL.withCause(ex.get()).asRuntimeException());
        send.cancel(true);
      }
    }

    void await() throws InterruptedException, ExecutionException {
      future.get();
    }

    List<Runnable> shutdownNow() {
      return exec.shutdownNow();
    }
  }

  final State CLOSED = new BaseState("CLOSED") {
    @Override
    public void onEnter(State prev) {
      closed = true;
    }
  };

  // --------------------------------------------------------------------------

  private final ScheduledExecutorService reconnectExec = Executors.newScheduledThreadPool(1);

  void scheduleReconnect(int reconnectIntervalSeconds) {
    reconnectExec.scheduleWithFixedDelay(() -> {
      if (Thread.currentThread().isInterrupted()) {
        onEvent(Event.SHUTTING_DOWN);
      }
      if (Thread.currentThread().isInterrupted()) {
        onEvent(Event.EOF);
      }

      // We want to count down from the moment we re-opened the stream,
      // not from the moment we initialited the sequence.
      lock.lock();
      try {
        while (state != ACTIVE) {
          stateChanged.await();
        }
      } catch (InterruptedException ignored) {
        // Let the process exit normally.
      } finally {
        lock.unlock();
      }
    }, reconnectIntervalSeconds, reconnectIntervalSeconds, TimeUnit.SECONDS);
  }
}
