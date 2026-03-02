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
import java.util.function.Predicate;

import javax.annotation.concurrent.GuardedBy;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.batch.Event.ClientError;
import io.weaviate.client6.v1.api.collections.batch.Event.StreamHangup;
import io.weaviate.client6.v1.api.collections.data.BatchReference;
import io.weaviate.client6.v1.api.collections.data.InsertManyRequest;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

/**
 * BatchContext stores the state of an active batch process
 * and controls its lifecycle.
 *
 * <h2>Lifecycle</h2>
 *
 * The SSB implementation is based on gRPC bidi-streams, which are modeled as a
 * pair of "observers" that define callbacks for inbound and outbound messages.
 * We'll refer to them as "sender" (sending messages to the server) and "recv"
 * (receiving and handing server-side events).
 *
 * <p>
 * When the context is started, the client exchanges a "recv" for a "sender",
 * then stores in {@link #messages}. A {@link Send} process is started in the
 * {@link #sendService} -- it will continue to run until the context is closed
 * either gracefully via {@link #close} or abruptly via {@link #shutdownNow}.
 *
 * <p>
 * A "recv" always runs on some internal gRPC thread. The "recv" process is
 * expected to exit whenever server closes its half of the stream, and will
 * be re-created if the stream is re-opened. {@link Recv} delegates most
 * of the operations to its parent BatchContext.
 *
 * <p>
 * {@link #send} and {@link #recv} futures track completion of the "sender"
 * and "recv" routines.
 *
 * <h2>State</h2>
 *
 * BatchContext organized client-side work using the
 * <a href= "https://refactoring.guru/design-patterns/state">State</a>
 * pattern. These states are defined:
 *
 * <ul>
 * <li>{@code null} -- context hasn't been {@link #start}ed yet. The context
 * SHOULD NOT be used in this state, as it will likely result in an NPE.
 * <li>AwaitStarted -- client's opened the stream, sent Start,
 * and is now awaiting for the server to respond with Started.
 * <li>Active -- the server is ready to accept the next Data message.
 * <li>InFlight -- the latest batch has been sent, awaiting Acks.
 * <li>OOM -- server has OOM'ed and will not accept any more data.
 * <li>ServerShuttingDown -- server's begun a graceful shutdown.
 * <li>Reconnecting -- server's closed it's half of the stream; the client
 * will try to reconnect to another instance up to {@link #maxReconnectRetries}
 * times.
 * </ul>
 *
 * <h2>Cancellation policy</h2>
 * BatchContext does not rely on timing heuristics advance its state.
 * Threads coordinate via {@link #stateChanged} conditional variable
 * and interrupts, when appropriate.
 *
 * <h3>Graceful shutdown</h3>
 * When {@link #close()} is called, the context will stop accepting
 * new items and start draining the remaining items in the {@link #queue}
 * and {@link #batch} backlog. The client will then continue processing
 * server-side events until stream's EOF. By the time context is closed
 * all submitted tasks are expected to be completed successfully or otherwise.
 *
 * <br>
 * N.B.: This may take an arbitrarily long amount time, as the client will
 * continue to re-connect to other instances and re-submit WIP tasks in
 * case the current stream is hung up or the server shuts down prematurely.
 *
 * <h3>Abrupt termination</h3>
 * In the event of an internal client error (e.g. in the "sender" or "recv"
 * threads), the client's half of the stream is closed immediately, and the
 * "sender" processed is cancelled. A subsequent call to {@link #close()} will
 * re-throw the causing exception as {@link IOException}. The stream can be
 * terminated at any time, including during a graceful shutdown.
 * In case the context if terminated <i>before</i> a graceful shutdown begins,
 * the parent thread is also interrupted to prevent {@link #add} from blocking
 * indefinitely, "sender" will not be there to pop items from the task queue).
 *
 * <p>
 * To prevent data loss, re-submit all incomplete tasks
 * to the next batch context.
 *
 * @param <PropertiesT> the shape of properties for inserted objects.
 *
 * @see StreamObserver
 * @see State
 * @see #shutdownNow
 * @see TaskHandle#result()
 *
 * @author Dyma Solovei
 */
public final class BatchContext<PropertiesT> implements Closeable {

  private final CollectionDescriptor<PropertiesT> collectionDescriptor;
  private final CollectionHandleDefaults collectionHandleDefaults;

  /**
   * Internal execution service. Its lifecycle is bound to that of the
   * BatchContext: it's started when the context is initialized
   * and shutdown on {@link #close}.
   *
   * <p>
   * In the event of abrupt stream termination ({@link Recv#onError} is called),
   * the "recv" thread MAY shutdown this service in order to interrupt the "send"
   * thread; the latter may be blocked on {@link Send#awaitCanSend} or
   * {@link Send#awaitCanPrepareNext}.
   */
  private final ExecutorService sendService = Executors.newSingleThreadExecutor();

  /**
   * Scheduled thread pool for delayed tasks.
   *
   * @see Oom
   * @see Reconnecting
   */
  private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1);

  /** The thread that created the context. */
  private final Thread parent = Thread.currentThread();

  /** Stream factory creates new streams. */
  private final StreamFactory<Message, Event> streamFactory;

  /**
   * Queue publishes insert tasks from the main thread to the "sender".
   *
   * <p>
   * Send {@link TaskHandle#POISON} to gracefully shut down the "sender"
   * thread. The same queue may be re-used with a different "sender",
   * e.g. after {@link #reconnect}, but only when the new thread is known
   * to have started. Otherwise, the thread trying to put an item on
   * the queue will block indefinitely.
   */
  private final BlockingQueue<TaskHandle> queue;

  /**
   * Work-in-progress items.
   *
   * <p>
   * An item is added to the wip map after the "sender" successfully
   * adds it to the {@link #batch} and is removed once the server reports
   * back the result (whether success of failure).
   */
  private final ConcurrentMap<String, TaskHandle> wip = new ConcurrentHashMap<>();

  /**
   * Current batch.
   *
   * <p>
   * An item is added to the batch after the "sender" pulls it
   * from the queue and remains there until it's Ack'ed.
   */
  private final Batch batch;

  /**
   * State encapsulates state-dependent behavior of the {@link BatchContext}.
   * Before reading state, a thread MUST acquire {@link #lock}.
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

  /**
   * Handle for the "sender" routine.
   * Cancel this future to interrupt the "sender".
   */
  private volatile Future<?> send;

  /**
   * Indicates completion of the "recv" routine.
   * Canceling this future will have no effect.
   */
  private volatile CompletableFuture<?> recv;

  /**
   * Maximum number of times the client will attempt to re-open the stream
   * before terminating the context.
   */
  private final int maxReconnectRetries;

  /** closing completes the stream. */
  private final CompletableFuture<Void> closing = new CompletableFuture<>();

  /** Executor for performing graceful shutdown sequence. */
  private final ExecutorService shutdownService = Executors.newSingleThreadExecutor();

  /** Lightweight check to ensure users cannot send on a closed context. */
  private volatile boolean closed;

  BatchContext(
      StreamFactory<Message, Event> streamFactory,
      int maxSizeBytes,
      CollectionDescriptor<PropertiesT> collectionDescriptor,
      CollectionHandleDefaults collectionHandleDefaults,
      int batchSize,
      int queueSize,
      int maxReconnectRetries) {
    this.streamFactory = requireNonNull(streamFactory, "streamFactory is null");
    this.collectionDescriptor = requireNonNull(collectionDescriptor, "collectionDescriptor is null");
    this.collectionHandleDefaults = requireNonNull(collectionHandleDefaults, "collectionHandleDefaults is null");

    this.queue = new ArrayBlockingQueue<>(queueSize);
    this.batch = new Batch(batchSize, maxSizeBytes);
    this.maxReconnectRetries = maxReconnectRetries;

    setState(AWAIT_STARTED);
  }

  private BatchContext(Builder<PropertiesT> builder) {
    this(
        builder.streamFactory,
        builder.maxSizeBytes,
        builder.collectionDescriptor,
        builder.collectionHandleDefaults,
        builder.batchSize,
        builder.queueSize,
        builder.maxReconnectRetries);
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
    if (closed) {
      throw new IllegalStateException("context is closed");
    }
    openStream();
    send = sendService.submit(new Send());
  }

  /**
   * Reconnect re-creates the stream and renews the {@link #recv} future.
   *
   * <p>
   * Unlike {@link #start} it does not trigger a state transition, and
   * {@link Reconnecting} will should continue to handle events until
   * the stream is renewed successfully or {@link #maxReconnectRetries}
   * is reached.
   */
  void reconnect() throws InterruptedException, ExecutionException {
    // The "sender" survives reconnects and will not call countDown
    // until it's interrupted or the context is closed.
    // The "recv" thread is guaranteed to have already exited, because
    // the context can only transition into the Reconnecting state
    // after the server half of the stream is closed (EOF or hangup).
    recv.get();
    openStream();
  }

  /**
   * Retry a task.
   *
   * <p>
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
    boolean closedBefore = closed;
    closed = true;

    if (!closedBefore) {
      System.out.println("CLOSE CONTEXT");
      shutdown();
    }

    try {
      closing.get();
    } catch (InterruptedException | ExecutionException e) {
      if (e instanceof InterruptedException ||
          e.getCause() instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new IOException(e.getCause());
    } finally {
      shutdownExecutors();
    }
  }

  /** Start a graceful context shutdown. */
  private void shutdown() {
    shutdownService.execute(() -> {
      try {
        // Poison the queue -- this will signal "send" to drain the remaining
        // items in the batch and in the backlog and exit.
        //
        // If shutdownNow has been called previously and the "send" routine
        // has been interrupted, this would block indefinitely.
        // Luckily, shutdownNow resolves the `closing` future as well.
        queue.put(TaskHandle.POISON);

        // Wait for both "send" to exit; "send" will not exit until "recv" completes.
        if (send != null) {
          send.get();
        }
        closing.complete(null);
      } catch (Exception e) {
        closing.completeExceptionally(e);
      }
    });
  }

  /** Terminate context abruptly. */
  private void shutdownNow(Exception ex) {
    // Now report this error to the server and terminate the stream.
    closing.completeExceptionally(ex);
    messages.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

    // Terminate the "send" routine.
    if (send != null) {
      send.cancel(true);
    }

    if (!closed) {
      // Since shutdownNow is never triggered by the "main" thread,
      // it may be blocked on trying to add to the queue. While batch
      // context is active, we own this thread and may interrupt it.
      parent.interrupt();
    }
  }

  private void shutdownExecutors() {
    sendService.shutdown();
    scheduledService.shutdown();
    shutdownService.shutdown();
  }

  /** Set the new state and notify awaiting threads. */
  void setState(State nextState) {
    requireNonNull(nextState, "nextState is null");

    lock.lock();
    System.out.println("setState " + state + " => " + nextState);
    try {
      State prev = state;
      state = nextState;
      state.onEnter(prev);
      stateChanged.signal();
    } finally {
      lock.unlock();
    }
  }

  /** Blocks until a change in {@link #state} causes the predicate to be true. */
  void awaitState(Predicate<State> predicate) throws InterruptedException {
    requireNonNull(predicate, "predicate is null");

    lock.lock();
    try {
      while (!predicate.test(state)) {
        stateChanged.await();
      }
    } finally {
      lock.unlock();
    }
  }

  /** Open a new batching stream. */
  void openStream() {
    Recv events = new Recv(this);
    recv = events;
    messages = streamFactory.createStream(events);
    messages.onNext(Message.start(collectionHandleDefaults.consistencyLevel()));
  }

  /** Close the client half of the stream. */
  void closeStream() {
    messages.onNext(Message.stop());
    messages.onCompleted();
  }

  /**
   * onEvent delegates event handling to {@link #state}.
   *
   * <p>
   * Be mindful that most of the time this callback will run in a hot path
   * on a gRPC thread. {@link State} implementations SHOULD offload any
   * blocking operations to one of the provided executors.
   *
   * @see #scheduledService
   */
  private void onEvent(Event event) {
    requireNonNull(event, "event is null");

    lock.lock();
    try {
      System.out.println("onEvent " + event);
      state.onEvent(event);
    } finally {
      lock.unlock();
    }
  }

  private TaskHandle add(final TaskHandle taskHandle) throws InterruptedException {
    if (closed) {
      throw new IllegalStateException("context is closed");
    }
    requireNonNull(taskHandle, "taskHandle is null");

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
      String threadName = Thread.currentThread().getName();
      Thread.currentThread().setName("sender");
      try {
        trySend();
      } finally {
        Thread.currentThread().setName(threadName);
      }
    }

    /**
     * trySend consumes {@link #queue} tasks and sends them in batches until it
     * encounters a {@link TaskHandle#POISON} or is otherwise interrupted.
     */
    private void trySend() {
      try {
        awaitState(State::canPrepareNext);

        while (!Thread.currentThread().isInterrupted()) {
          if (batch.isFull()) {
            System.out.println("==[send batch]==>");
            send();
          }

          TaskHandle task = queue.take();

          if (task == TaskHandle.POISON) {
            System.out.println("took POISON");
            drain();

            closeStream();

            // The SSB protocol requires the client to continue reading the stream
            // until EOF. In the happy case, the server will close its half having
            // processed all previous requests; the WIP buffer is empty in that case.
            //
            // It is possible that the server will be restarted or the stream will be
            // hungup before client receives all Results, in which case we might need
            // to re-submit the items remaining in the WIP buffer.
            recv.get();

            assert closed : "queue poisoned when context not closed";
            if (wip.isEmpty()) {
              return;
            }

            // Server closed the stream before reporting all expected Results.
            // Return the poison to the queue and go another round -- if the
            // client tried to reconnect the batch may've been filled again.
            assert queue.isEmpty() : "queue must be empty after poison";
            queue.add(task);
            continue;
          }

          Data data = task.data();
          batch.add(data);

          TaskHandle existing = wip.put(task.id(), task);
          assert existing == null : "duplicate tasks in progress, id=" + existing.id();
        }
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        onEvent(new Event.ClientError(e));
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

    /**
     * Block until the current state allows {@link State#canSend},
     * then prepare the batch, send it, and set InFlight state.
     * Block until the current state allows {@link State#canPrepareNext}.
     *
     * <br>
     * Depending on the BatchContext lifecycle, the semantics of
     * "await can prepare next" can be one of "message is ACK'ed"
     * "the stream has started", or, more generally,
     * "it is safe to take a next item from the queue and add it to the batch".
     *
     * @see Batch#prepare
     * @see #IN_FLIGHT
     */
    private void flush() throws InterruptedException {
      awaitState(State::canSend);
      messages.onNext(batch.prepare());
      setState(IN_FLIGHT);
      awaitState(State::canPrepareNext);
    }

  }

  private static final class Recv extends CompletableFuture<Void> implements StreamObserver<Event> {
    private final BatchContext<?> context;

    private Recv(BatchContext<?> context) {
      this.context = context;
    }

    @Override
    public void onNext(Event event) {
      try {
        context.onEvent(event);
      } catch (Exception e) {
        context.onEvent(new Event.ClientError(e));
      }
    }

    /**
     * EOF for the server-side stream.
     * By the time this is called, the client-side of the stream had been closed
     * and the "send" thread has either exited or is on its way there.
     */
    @Override
    public void onCompleted() {
      try {
        context.onEvent(Event.EOF);
      } finally {
        complete(null);
      }
    }

    /** An exception occurred either on our end or in the channel internals. */
    @Override
    public void onError(Throwable t) {
      try {
        context.onEvent(Event.StreamHangup.fromThrowable(t));
      } finally {
        complete(null);
      }
    }
  }

  private final State AWAIT_STARTED = new BaseState("AWAIT_STARTED", BaseState.Action.PREPARE_NEXT);
  private final State ACTIVE = new BaseState("ACTIVE", BaseState.Action.PREPARE_NEXT, BaseState.Action.SEND);
  private final State IN_FLIGHT = new BaseState("IN_FLIGHT");

  /** BaseState implements default handlers for all {@link Event} subclasses. */
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
      this.permitted = requireNonNull(permitted, "actions is null").length == 0
          ? EnumSet.noneOf(Action.class)
          : EnumSet.copyOf(Arrays.asList(permitted));
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
    public void onEvent(Event event) {
      if (event == Event.STARTED) {
        onStarted();
      } else if (event instanceof Event.Acks acks) {
        onAcks(acks);
      } else if (event instanceof Event.Oom oom) {
        onOom(oom);
      } else if (event instanceof Event.Results results) {
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
        throw new AssertionError("unreachable with event " + event);
      }
    }

    private void onStarted() {
      setState(ACTIVE);
    }

    private void onAcks(Event.Acks acks) {
      Collection<String> removed = batch.clear();
      if (!acks.acked().containsAll(removed)) {
        throw ProtocolViolationException.incompleteAcks(List.copyOf(removed));
      }
      acks.acked().forEach(id -> {
        TaskHandle task = wip.get(id);
        if (task != null) {
          task.setAcked();
        }
      });
      setState(ACTIVE);
    }

    private void onResults(Event.Results results) {
      results.successful().forEach(id -> wip.remove(id).setSuccess());
      results.errors().forEach((id, error) -> wip.remove(id).setError(error));
    }

    private void onBackoff(Event.Backoff backoff) {
      batch.setMaxSize(backoff.maxSize());
    }

    private void onOom(Event.Oom oom) {
      setState(new Oom(oom.delaySeconds()));
    }

    private void onShuttingDown() {
      setState(new ServerShuttingDown(this));
    }

    private void onStreamClosed(Event event) {
      if (event instanceof Event.StreamHangup hangup) {
        hangup.exception().printStackTrace();
      }
      if (!send.isDone()) {
        setState(new Reconnecting(maxReconnectRetries));
      }
    }

    private void onClientError(Event.ClientError error) {
      shutdownNow(error.exception());
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Oom waits for {@link Event#SHUTTING_DOWN} up to a specified amount of time,
   * after which it will force stream termination by imitating server shutdown.
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
      shutdown = scheduledService.schedule(this::initiateShutdown, delaySeconds, TimeUnit.SECONDS);
    }

    /** Imitate server shutdown sequence. */
    private void initiateShutdown() {
      // We cannot route event handling via normal BatchContext#onEvent, because
      // it delegates to the current state, which is Oom. If Oom#onEvent were to
      // receive an Event.SHUTTING_DOWN, it would cancel this execution of this
      // very sequence. Instead, we delegate to our parent BaseState which normally
      // handles these events.
      if (!Thread.currentThread().isInterrupted()) {
        BatchContext.this.onEvent(Event.SHUTTING_DOWN);
      }
      if (!Thread.currentThread().isInterrupted()) {
        BatchContext.this.onEvent(Event.EOF);
      }
    }

    @Override
    public void onEvent(Event event) {
      if (event == Event.SHUTTING_DOWN ||
          event instanceof StreamHangup ||
          event instanceof ClientError) {
        shutdown.cancel(true);
        try {
          shutdown.get();
        } catch (CancellationException ignored) {
        } catch (InterruptedException ignored) {
          // "recv" is running on a thread from gRPC's internal thread pool,
          // so, while onEvent allows InterruptedException to stay responsive,
          // in practice this thread will only be interrupted by the thread pool,
          // which already knows it's being shut down.
        } catch (ExecutionException ex) {
          onEvent(new Event.ClientError(ex));
        }
      }
      super.onEvent(event);
    }
  }

  /**
   * ServerShuttingDown allows preparing the next batch
   * unless the server's OOM'ed on the previous one.
   * Once set, the state will shutdown {@link BatchContext#sendService}
   * to instruct the "send" thread to close our part of the stream.
   */
  private final class ServerShuttingDown extends BaseState {
    private final boolean canPrepareNext;

    private ServerShuttingDown(State previous) {
      super("SERVER_SHUTTING_DOWN");
      this.canPrepareNext = previous == null || !Oom.class.isAssignableFrom(previous.getClass());
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
      closeStream();
    }
  }

  /**
   * Reconnecting state is entered either by the server finishing a shutdown
   * and closing its end of the stream or an unexpected stream hangup.
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
      // The reconnected state is re-set every time the stream restarts.
      // This ensures that onEnter hook is only called the first
      // time we enter Reconnecting state.
      if (prev == this) {
        return;
      }

      if (!ServerShuttingDown.class.isAssignableFrom(prev.getClass())) {
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
          onEvent(new Event.ClientError(new IOException("Server unavailable")));
        } else {
          reconnectAfter(2 ^ retries);
        }
      } else {
        super.onEvent(event);
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
     * <p>
     * The task is scheduled on {@link #scheduledService} even if
     * {@code delaySeconds == 0} to avoid blocking gRPC worker
     * thread,
     * where the {@link BatchContext#onEvent} callback runs.
     *
     * @param delaySeconds Delay in seconds.
     */
    private void reconnectAfter(long delaySeconds) {
      retries++;

      scheduledService.schedule(() -> {
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

  // --------------------------------------------------------------------------

  private final ScheduledExecutorService reconnectExec = Executors.newScheduledThreadPool(1);

  void scheduleReconnect(int reconnectIntervalSeconds) {
    reconnectExec.scheduleWithFixedDelay(() -> {
      if (!Thread.currentThread().isInterrupted()) {
        onEvent(Event.SHUTTING_DOWN);
      }
      if (!Thread.currentThread().isInterrupted()) {
        onEvent(Event.EOF);
      }

      // We want to count down from the moment we re-opened the stream,
      // not from the moment we initialized the sequence.
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

  public static class Builder<PropertiesT> implements ObjectBuilder<BatchContext<PropertiesT>> {
    private final StreamFactory<Message, Event> streamFactory;
    private final int maxSizeBytes;
    private final CollectionDescriptor<PropertiesT> collectionDescriptor;
    private final CollectionHandleDefaults collectionHandleDefaults;

    Builder(
        StreamFactory<Message, Event> streamFactory,
        int maxSizeBytes,
        CollectionDescriptor<PropertiesT> collectionDescriptor,
        CollectionHandleDefaults collectionHandleDefaults) {
      this.streamFactory = streamFactory;
      this.maxSizeBytes = maxSizeBytes;
      this.collectionDescriptor = collectionDescriptor;
      this.collectionHandleDefaults = collectionHandleDefaults;
    }

    private int batchSize = 1_000;
    private int queueSize = 1_000;
    private int maxReconnectRetries = 5;

    public Builder<PropertiesT> batchSize(int batchSize) {
      this.batchSize = batchSize;
      return this;
    }

    public Builder<PropertiesT> queueSize(int queueSize) {
      this.queueSize = queueSize;
      return this;
    }

    public Builder<PropertiesT> maxReconnectRetries(int maxReconnectRetries) {
      this.maxReconnectRetries = maxReconnectRetries;
      return this;
    }

    @Override
    public BatchContext<PropertiesT> build() {
      return new BatchContext<>(this);
    }
  }
}
