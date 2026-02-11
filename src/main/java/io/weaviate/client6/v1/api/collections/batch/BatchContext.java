package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
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
   */
  private final ExecutorService exec = Executors.newSingleThreadExecutor();

  /** Service thread pool for OOM timer. */
  private final ScheduledExecutorService scheduledExec = Executors.newScheduledThreadPool(1);

  /**
   * Currently open stream. This will be created on {@link #start}.
   * Other threads MAY use stream but MUST NOT update this field on their own.
   */
  private volatile StreamObserver<Message> messages;

  private volatile StreamObserver<Event> events;

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
    Recv recv = new Recv();
    messages = streamFactory.createStream(recv);
    events = recv;

    Send send = new Send();
    exec.execute(send);
  }

  void reconnect() {
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

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'close'");
  }

  /** Set the new state and notify awaiting threads. */
  void setState(State nextState) {
    requireNonNull(nextState, "nextState is null");

    lock.lock();
    try {
      state = nextState;
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
    // TODO(dyma): check that we haven't closed the stream on our end yet
    // probably with some state.isClosed() or something
    // TODO(dyma): check that wip doesn't have that ID yet, otherwise
    // we can lose some data (results)
    queue.put(taskHandle);
    return taskHandle;
  }

  private final class Send implements Runnable {
    @Override
    public void run() {
      try {
        // trySend exists normally
        trySend();
        messages.onCompleted();
        return;
      } catch (InterruptedException ignored) {
        // TODO(dyma): interrupted (whether through the exception
        // by breaking the while loop. Restore the interrupted status
        // and update the state
      }
    }

    private void trySend() throws InterruptedException {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          // if batch is full:
          // -> if the stream is closed / status is error (error) return
          // else send and await ack
          //
          // take the next item in the queue
          // -> if POISON: drain the batch, call onComplete, return
          //
          // add to batch

          // TODO(dyma): check that the batch is
          if (batch.isFull()) {
            send();
          }

          TaskHandle task = queue.take();

          if (task == TaskHandle.POISON) {
            drain();
            return;
          }

          Data data = task.data();
          if (!batch.add(data)) {
            // FIXME(dyma): once we've removed a task from the queue, we must
            // ensure that it makes it's way to the batch, otherwise we lose
            // that task. Here, for example, send() can be interrupted in which
            // case the task is lost.
            // How do we fix this? We cannot ignore the interrupt, because
            // interrupted send() means the batch was not acked, so it will
            // not accept any new items.
            //
            // Maybe! batch.add should put the data in the backlog if it couldn't
            // fit it in the buffer!!!
            // Yes!!!!!!! The backlog is not limited is size, so it will fit any
            // data that does not exceed maxGrpcMessageSize. We wouldn't need to
            // do a second pass ourselves.
            send();
            boolean ok = batch.add(data);
            assert ok : "batch.add must succeed after send";
          }

          // TODO(dyma): check that the previous is null,
          // we should've checked for that upstream in add().
          TaskHandle existing = wip.put(task.id(), task);
          assert existing == null : "duplicate tasks in progress, id=" + existing.id();
        }
      } catch (DataTooBigException e) {
        // TODO(dyma): fail
      }
    }

    private void send() throws InterruptedException {
      // This will stop sending as soon as we get the batch not a "not full" state.
      // The reason we do that is to account for the backlog, which might re-fill
      // the batch's buffer after .clear().
      while (batch.isFull()) {
        flush();
      }
      assert !batch.isFull() : "batch is full after send";
    }

    private void drain() throws InterruptedException {
      // This will send until ALL items in the batch have been sent.
      while (!batch.isEmpty()) {
        flush();
      }
      assert batch.isEmpty() : "batch not empty after drain";
    }

    private void flush() throws InterruptedException {
      // TODO(dyma): if we're in OOM / ServerShuttingDown state, then we known there
      // isn't any reason to keep waiting for the acks. However, we cannot exit
      // without taking a poison pill from the queue, because this risks blocking the
      // producer thread.
      // So we patiently wait, relying purely on the 2 booleans: canSend and
      // "isAcked". maybe not "isAcked" but "canAdd" / "canAccept"?

      // FIXME(dyma): draining the batch is not a good idea because the backlog
      // is likely smaller that the maxSize, so we'd sending half-empty batches.

      awaitCanSend();
      messages.onNext(batch.prepare());
      setState(AWAIT_ACKS);
      awaitAcked(); // TODO(dyma): rename canTake into something like awaitAcked();
      // the method can be called boolean isInFlight();
    }

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

    // TODO(dyma): the semantics of "canTake" is rather "can I put more data in the
    // batch", even more precisely -- "is the batch still in-flight or is it open?"
    private void awaitAcked() throws InterruptedException {
      lock.lock();
      try {
        while (!state.canTake()) {
          stateChanged.await();
        }
      } finally {
        // Not a good assertion: batch could've been re-populated from the backlog.
        // assert !batch.isFull() : "take allowed with full batch";
        lock.unlock();
      }
    }
  }

  private final class Recv implements StreamObserver<Event> {

    @Override
    public void onNext(Event event) {
      try {
        BatchContext.this.onEvent(event);
      } catch (InterruptedException e) {
        // TODO(dyma): cancel the RPC (req.onError())
      } catch (Exception e) {
        // TODO(dyma): cancel with
      }
    }

    @Override
    public void onCompleted() {
      // TODO(dyma): server closed its side of the stream successfully
      // Maybe log, but there's nothing that we need to do here
      // This is the EOF that the protocol document is talking about
    }

    @Override
    public void onError(Throwable arg0) {
      // TODO(dyma): if we did req.onError(), then the error can be ignored
      // The exception should be set somewhere so all threads can observe it
    }
  }

  final State CLOSED = new BaseState();
  final State AWAIT_STARTED = new BaseState(BaseState.Action.TAKE) {
    @Override
    public void onEvent(Event event) {
      if (requireNonNull(event, "event is null") == Event.STARTED) {
        setState(ACTIVE);
        return;
      }
      super.onEvent(event);
    }
  };
  final State ACTIVE = new BaseState(BaseState.Action.TAKE, BaseState.Action.SEND);
  final State AWAIT_ACKS = new BaseState() {
    @Override
    public void onEvent(Event event) {
      requireNonNull(event, "event is null");

      if (event instanceof Event.Acks acks) {
        Collection<String> remaining = batch.clear();
        if (!remaining.isEmpty()) {
          // TODO(dyma): throw an exception -- this is bad
        }
        // TODO(dyma): should we check if wip contains ID?
        // TODO(dyma): do we need to synchronize here? I don't think so...
        acks.acked().forEach(ack -> wip.get(ack).setAcked());
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
    private final EnumSet<Action> permitted;

    enum Action {
      TAKE, SEND;
    }

    protected BaseState(Action... actions) {
      this.permitted = EnumSet.copyOf(Arrays.asList(requireNonNull(actions, "actions is null")));
    }

    @Override
    public boolean canSend() {
      return permitted.contains(Action.SEND);
    }

    @Override
    public boolean canTake() {
      return permitted.contains(Action.TAKE);
    }

    @Override
    public void onEvent(Event event) {
      requireNonNull(event, "event is null");

      if (event instanceof Event.Results results) {
        results.successful().forEach(id -> wip.get(id).setSuccess());
        results.errors().forEach((id, error) -> wip.get(id).setError(error));
      } else if (event instanceof Event.Backoff backoff) {
        batch.setMaxSize(backoff.maxSize());
      } else if (event == Event.SHUTTING_DOWN) {
        setState(new ServerShuttingDown(this));
      } else {
        throw new IllegalStateException("cannot handle " + event.getClass());
      }
    }
  }

  private class Oom extends BaseState {
    private final ScheduledFuture<?> shutdown;

    private Oom(long delaySeconds) {
      super();
      this.shutdown = scheduledExec.schedule(this::initiateShutdown, delaySeconds, TimeUnit.SECONDS);
    }

    private void initiateShutdown() {
      if (Thread.currentThread().isInterrupted()) {
        return;
      }
      events.onNext(Event.SHUTTING_DOWN);
      events.onNext(Event.SHUTDOWN);
    }

    @Override
    public void onEvent(Event event) {
      if (requireNonNull(event, "event is null") != Event.SHUTTING_DOWN) {
        throw new IllegalStateException("Expected OOM to be followed by ShuttingDown");
      }

      shutdown.cancel(true);
      setState(new ServerShuttingDown(this));
    }
  }

  private class ServerShuttingDown implements State {
    private final boolean canTake;

    private ServerShuttingDown(State previous) {
      this.canTake = requireNonNull(previous, "previous is null").getClass() == Oom.class;
    }

    @Override
    public boolean canTake() {
      return canTake;
    }

    @Override
    public boolean canSend() {
      return false;
    }

    @Override
    public void onEvent(Event event) throws InterruptedException {
      if (requireNonNull(event, "event is null") != Event.SHUTDOWN) {
        throw new IllegalStateException("Expected ShuttingDown to be followed by Shutdown");
      }
      setState(CLOSED);
    }
  }
}
