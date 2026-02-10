package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;

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

  private final StreamFactory<Message, Event> streamFactory;
  private final Optional<ConsistencyLevel> consistencyLevel;

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

  BatchContext(
      StreamFactory<Message, Event> streamFactory,
      int maxSizeBytes,
      Optional<ConsistencyLevel> consistencyLevel) {
    this.streamFactory = requireNonNull(streamFactory, "streamFactory is null");
    this.consistencyLevel = requireNonNull(consistencyLevel, "consistencyLevel is null");

    this.queue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_SIZE);
    this.batch = new Batch(DEFAULT_BATCH_SIZE, maxSizeBytes);
  }

  void start() {

  }

  void reconnect() {
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

  boolean canSend() {
    lock.lock();
    try {
      return state.canSend();
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

  /** Add {@link WeaviateObject} to the batch. */
  public TaskHandle add() {
    return null;
  }

  public TaskHandle retry(TaskHandle taskHandle) {
    return null;
  }

  private final class Sender implements Runnable {
    private final StreamObserver<Message> stream;

    private Sender(StreamObserver<Message> stream) {
      this.stream = requireNonNull(stream, "stream is null");
    }

    @Override
    public void run() {
      throw new UnsupportedOperationException("implement!");
    }
  }

  private final class Recv implements StreamObserver<Event> {

    @Override
    public void onCompleted() {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'onCompleted'");
    }

    @Override
    public void onError(Throwable arg0) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    @Override
    public void onNext(Event arg0) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'onNext'");
    }
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'close'");
  }
}
