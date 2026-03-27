package io.weaviate.client6.v1.api.collections.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class BatchContextTest {
  private static final Logger log = LoggerFactory.getLogger(BatchContextTest.class);

  private static final CollectionDescriptor<Map<String, Object>> DESCRIPTOR = CollectionDescriptor
      .ofMap("BatchContextTest");
  private static final CollectionHandleDefaults DEFAULTS = new CollectionHandleDefaults(
      Optional.of(ConsistencyLevel.ONE), Optional.of("john_doe"));

  /**
   * Maximum gRPC message size of 2KB .
   * 1KB is {@link MessageSizeUtil#SAFETY_MARGIN}.
   */
  private static final int MAX_SIZE_BYTES = 2 * 1024;
  private static final int BATCH_SIZE = 10;
  private static final int QUEUE_SIZE = 1;
  private static final int MAX_RECONNECT_RETRIES = 1;
  private static final RetryPolicy RETRY_POLICY = new RetryPolicy(1);

  /**
   * This test models the client-side part of the stream as a blocking queue.
   * All streams created by {@link #createStream} are backed by the same queue,
   * which "survives" reconnects. The test code can listen on the same stream
   * even if the batch client re-creates it.
   */
  private final BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> REQUEST_QUEUE = new ArrayBlockingQueue<>(1);

  /**
   * Dedicated executor for occasional asynchrony.
   *
   * <p>
   * Most of the time the "server", the "user", the "test" processes
   * will run on the main thread, one at a time, when the logic permits.
   * In some cases it is useful to put at least one of those processes
   * onto a separate thread. E.g. when the batch is being drained, but
   * the main thread is blocked by {@link BatchContext#close}.
   */
  private ExecutorService backgroundThread;

  /**
   * Server-side events must be emitted from a dedicated thread, to avoid
   * deadlocks between the test code and the client side-effects we expect
   * to take place and await.
   */
  private ExecutorService eventThread;

  /**
   * Batch context for the current test case.
   * Only {@link #startContext()} should assign to context.
   */
  private volatile BatchContext<Map<String, Object>> context;

  /** Track if the context has already been closed inside of the test. */
  private boolean contextClosed;

  /** Server half of the stream. */
  private volatile OutboundStream out;
  /** Client half of the stream. */
  private volatile InboundStream in;

  private StreamObserver<Message> createStream(StreamObserver<Event> recv) {
    out = new OutboundStream(recv, eventThread);
    in = new InboundStream(REQUEST_QUEUE, out);
    return in;
  }

  @Rule
  public TestName currentTest = new TestName();

  private boolean testFailed;

  @Rule
  public TestWatcher __ = new TestWatcher() {
    @Override
    protected void failed(Throwable e, Description description) {
      testFailed = true;
    }
  };

  /**
   * Create new unstarted context with default maxSizeBytes, collection
   * descriptor, and collection handle defaults.
   */
  @Before
  public void startContext() throws InterruptedException {
    log.debug("===================startContext==================");
    log.debug(currentTest.getMethodName());

    assert !Thread.currentThread().isInterrupted() : "main thread interrupted";
    assert REQUEST_QUEUE.isEmpty() : "stream contains incoming message " + REQUEST_QUEUE.peek();

    backgroundThread = Executors.newSingleThreadExecutor();
    eventThread = Executors.newSingleThreadExecutor();

    context = new BatchContext.Builder<>(this::createStream, MAX_SIZE_BYTES, DESCRIPTOR, DEFAULTS)
        .batchSize(BATCH_SIZE)
        .queueSize(QUEUE_SIZE)
        .maxReconnectRetries(MAX_RECONNECT_RETRIES)
        .retryPolicy(RETRY_POLICY)
        .build();
    context.start();

    in.expectMessage(START);
    out.emitEventAsync(Event.STARTED);
  }

  @After
  public void reset() throws Exception {
    log.atDebug()
        .addKeyValue("contextClosed", contextClosed)
        .addKeyValue("testFailed", testFailed)
        .log("Begin test cleanup");

    // Do not attempt to close the context if it has been previously closed
    // by the test or the test has failed. In the latter case closing the
    // context may lead to a deadlock if the case hasn't scheduled Results
    // for all submitted messages.
    if (!contextClosed && !testFailed) {
      closeContext();
    }

    context = null;
    in = null;
    out = null;

    // This resets the interrupted flag, allowing use to await the executors.
    Thread.interrupted();

    try {
      backgroundThread.shutdownNow();
      backgroundThread.awaitTermination(10, TimeUnit.SECONDS);

      eventThread.shutdownNow();
      eventThread.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException ignored) {
    }

    REQUEST_QUEUE.clear();

    log.debug("===================closeContext==================");
  }

  private static final WeaviateProtoBatch.BatchStreamRequest.MessageCase START = WeaviateProtoBatch.BatchStreamRequest.MessageCase.START;
  private static final WeaviateProtoBatch.BatchStreamRequest.MessageCase STOP = WeaviateProtoBatch.BatchStreamRequest.MessageCase.STOP;
  private static final WeaviateProtoBatch.BatchStreamRequest.MessageCase DATA = WeaviateProtoBatch.BatchStreamRequest.MessageCase.DATA;

  private void closeContext() throws Exception {
    CompletableFuture<Void> eof = CompletableFuture.runAsync(() -> {
      try {
        in.expectMessage(STOP);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }, backgroundThread).thenCompose(__ -> out.eof(true));

    try {
      context.close();
      eof.get(5, TimeUnit.SECONDS);
    } finally {
      contextClosed = true;
    }
  }

  @Test
  public void test_sendOneBatch() throws Exception {
    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // BatchContext should flush the current batch once it hits its limit.
    // We will ack all items in the batch and send successful result for each one.
    List<String> received = recvDataAndAck();
    out.emitEventAsync(new Event.Results(received, Collections.emptyMap()));

    Assertions.assertThat(tasks)
        .extracting(TaskHandle::id).containsExactlyInAnyOrderElementsOf(received);

    // Since MockServer runs in the same thread as this test,
    // the context will be updated before the last emitEvent returns.
    closeContext();

    // By the time context.close() returns all tasks MUST have results set.
    Assertions.assertThat(tasks).extracting(TaskHandle::done)
        .allMatch(CompletableFuture::isDone)
        .noneMatch(CompletableFuture::isCompletedExceptionally);
  }

  @Test
  public void test_drainOnClose() throws Exception {
    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 2; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Contrary the test above, we expect the objects to be sent
    // only after context.close(), as the half-empty batch will
    // be drained. Similarly, we want to ack everything as it arrives.
    backgroundThread.submit(() -> {
      try {
        List<String> received = recvDataAndAck();
        Assertions.assertThat(tasks).extracting(TaskHandle::id)
            .containsExactlyInAnyOrderElementsOf(received);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    out.emitEventAsync(new Event.Results(submitted, Collections.emptyMap()));

    closeContext();

    Assertions.assertThat(tasks).extracting(TaskHandle::done)
        .allMatch(CompletableFuture::isDone)
        .noneMatch(CompletableFuture::isCompletedExceptionally);
  }

  @Test
  public void test_backoff() throws Exception {
    out.emitEvent(new Event.Backoff(BATCH_SIZE / 2));

    List<TaskHandle> tasks = new ArrayList<>();
    Future<?> backgroundAdd = backgroundThread.submit(() -> {
      try {
        for (int i = 0; i < BATCH_SIZE; i++) {
          tasks.add(context.add(WeaviateObject.of()));
        }
      } catch (InterruptedException e) {
        throw new RuntimeException("test user interrupted", e);
      }
    });

    // BatchContext should flush the current batch once it hits the limit
    // set by the Backoff message, i.e. BATCH_SIZE / 2.
    List<String> received = recvDataAndAck();
    Assertions.assertThat(received).hasSize(BATCH_SIZE / 2);
    out.emitEventAsync(new Event.Results(received, Collections.emptyMap()));

    backgroundAdd.get(); // Finish populating batch context.

    // Since testUser will try and add BATCH_SIZE no. objects,
    // we should expect there to be exactly 2 batches.
    received = recvDataAndAck();
    Assertions.assertThat(received).hasSize(BATCH_SIZE / 2);
    out.emitEventAsync(new Event.Results(received, Collections.emptyMap()));

    closeContext();

    Assertions.assertThat(tasks).extracting(TaskHandle::done)
        .allMatch(CompletableFuture::isDone)
        .noneMatch(CompletableFuture::isCompletedExceptionally);
  }

  @Test
  public void test_backoffBacklog() throws Exception {
    // Pre-fill the batch without triggering a flush (n-1).
    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    int batchSizeNew = BATCH_SIZE / 2;

    // Force the last BATCH_SIZE / 2 - 1 items to be transferred to the backlog.
    // Await for this event to be processed before moving forward.
    out.emitEvent(new Event.Backoff(batchSizeNew));

    // The next item will go on the backlog and the trigger a flush,
    // which will continue to send batches and re-populate from
    // the backlog as long as the batch is full, so we should expect
    // to see 2 batches of size BATCH_SIZE / 2 each.
    List<String> received;
    tasks.add(context.add(WeaviateObject.of()));

    Assertions.assertThat(received = recvDataAndAck()).hasSize(batchSizeNew);
    out.emitEventAsync(new Event.Results(received, Collections.emptyMap()));

    Assertions.assertThat(received = recvDataAndAck()).hasSize(batchSizeNew);
    out.emitEventAsync(new Event.Results(received, Collections.emptyMap()));

    closeContext();

    Assertions.assertThat(tasks).extracting(TaskHandle::done)
        .allMatch(CompletableFuture::isDone)
        .noneMatch(CompletableFuture::isCompletedExceptionally);
  }

  @Test
  public void test_reconnect_onShutdown() throws Exception {
    out.emitEventAsync(Event.SHUTTING_DOWN);
    in.expectMessage(STOP);
    out.eof(true);
    in.expectMessage(START);

    // Not strictly necessary -- we can close the context
    // before a new connection is established.
    out.emitEventAsync(Event.STARTED);
  }

  @Test
  public void test_reconnect_onOom() throws Exception {
    List<TaskHandle> tasks = new ArrayList<>();

    // OOM is the opposite of Ack -- trigger a flush first.
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Respond with OOM and wait for the client to close its end of the stream.
    in.expectMessage(DATA);
    out.emitEventAsync(new Event.Oom(0));

    // Close the server's end of the stream.
    in.expectMessage(STOP);

    // Allow the client to reconnect to another "instance" and Ack the batch.
    in.expectMessage(START);
    out.emitEventAsync(Event.STARTED);
    recvDataAndAck();

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    out.emitEventAsync(new Event.Results(submitted, Collections.emptyMap()));
  }

  @Test
  public void test_reconnect_onStreamHangup() throws Exception {
    List<TaskHandle> tasks = new ArrayList<>();
    // Trigger a flush.
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Expect a new batch to arrive. Hangup the stream before sending the Acks.
    in.expectMessage(DATA);
    out.hangup();

    // The client should try to reconnect, because the context is still open.
    in.expectMessage(START);
    out.emitEventAsync(Event.STARTED);

    // The previous batch hasn't been acked, so we should expect to receive it
    // again.
    recvDataAndAck();

    // After the last batch has been acked, the Sender waits to take the next
    // item from the queue. Hangup the stream again, and add put another object
    // in the queue to wake the sender up.
    out.hangup();
    in.expectMessage(START);
    out.emitEventAsync(Event.STARTED);
    tasks.add(context.add(WeaviateObject.of()));
    recvDataAndAck();

    // Now fill up the rest of the batch to trigger a flush. Ack the incoming batch.
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }
    recvDataAndAck();

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    log.info("Will send results for {} items before EOF", submitted.size());
    out.emitEventAsync(new Event.Results(submitted, Collections.emptyMap()));
  }

  @Test
  public void test_reconnect_DrainAfterStreamHangup() throws Exception {
    List<TaskHandle> tasks = new ArrayList<>();

    // Trigger a flush.
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Expect a new batch to arrive. Ack the batch, and trigger another flush.
    recvDataAndAck();
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Ack the latest batch, but now Before sending back the results
    // for either one, hang up the stream.
    // On hangup, client should re-populate the batch from the WIP buffer.
    // Add one more item to overflow, so that there are now 3 pending batches.
    recvDataAndAck();
    out.hangup();
    tasks.add(context.add(WeaviateObject.of()));

    // The client will try to reconnect, because the context is still open.
    // When the server starts accepting connections again, the client should
    // drain the remaining BATCH_SIZE+1 objects as we close the context.
    in.expectMessage(START);
    out.emitEventAsync(Event.STARTED);
    Future<?> backgroundAcks = backgroundThread.submit(() -> {
      try {
        recvDataAndAck();
        recvDataAndAck();
        recvDataAndAck();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    out.emitEventAsync(new Event.Results(submitted, Collections.emptyMap()));

    closeContext();
    backgroundAcks.get();

    Assertions.assertThat(tasks).extracting(TaskHandle::done)
        .allMatch(CompletableFuture::isDone)
        .noneMatch(CompletableFuture::isCompletedExceptionally);
  }

  @Test
  public void test_retryPolicy() throws Exception {
    List<TaskHandle> tasks = new ArrayList<>();

    // Trigger a flush.
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Expect a new batch to arrive. Ack the batch, and add BATCH_SIZE - 1 items.
    recvDataAndAck();
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // These will be arriving after the context starts to close.
    backgroundThread.execute(() -> {
      Assertions.assertThatCode(() -> {
        // Expect the (BATCH_SIZE - 1) batch to arrive.
        recvDataAndAck();

        // Return success for the first (BATCH_SIZE - 1) items
        // and errors for the next (BATCH_SIZE - 1) items.
        List<String> successful = tasks.subList(0, BATCH_SIZE - 1).stream()
            .map(TaskHandle::id).toList();
        Map<String, String> errors = tasks.subList(BATCH_SIZE - 1, (BATCH_SIZE - 1) * 2).stream()
            .collect(Collectors.toMap(TaskHandle::id, __ -> "Whaam!"));
        out.emitEventAsync(new Event.Results(successful, errors));

        // Now WIP should contain exactly (BATCH_SIZE - 2) items
        // and the batch (BATCH_SIZE - 1) retried items.
        // Return another error an await a full batch of retried items to arrive.
        TaskHandle lastTask = tasks.get(tasks.size() - 1);
        out.emitEventAsync(new Event.Results(Collections.emptyList(), Map.of(lastTask.id(), "Whaam!")));

        List<String> retried = recvDataAndAck();

        // Fail all retried tasks. After this context.close() should unblock.
        errors = retried.stream().collect(Collectors.toMap(Function.identity(), __ -> "Whaam!"));
        out.emitEventAsync(new Event.Results(Collections.emptyList(), errors));
      }).doesNotThrowAnyException();
    });

    // Close the context.
    closeContext();

    Assertions.assertThat(tasks.subList(0, BATCH_SIZE - 1))
        .as("first %d tasks succeeded", BATCH_SIZE - 1)
        .extracting(TaskHandle::done)
        .allMatch(CompletableFuture::isDone)
        .noneMatch(CompletableFuture::isCompletedExceptionally);

    Assertions.assertThat(tasks.subList(BATCH_SIZE - 1, BATCH_SIZE))
        .as("last %d tasks failed", BATCH_SIZE)
        .extracting(TaskHandle::done)
        .allMatch(CompletableFuture::isCompletedExceptionally);

    Assertions.assertThat(context.numberOfErrors())
        .as("number of errors")
        .isEqualTo(BATCH_SIZE);
  }

  @Test
  public void test_closeAfterStreamHangup() throws Exception {
    out.hangup();
    in.expectMessage(START);
    out.emitEventAsync(Event.STARTED);
  }

  @Test
  public void test_maxReconnectRetries() throws Exception {
    // Drop the connection several times until the client
    // exhausts its reconnect attempts.
    int retries = 0;
    while (retries < MAX_RECONNECT_RETRIES) {
      out.hangup();
      in.expectMessage(START);
      retries++;
    }

    out.hangup();
    Assertions.assertThatThrownBy(this::closeContext)
        .isInstanceOf(IOException.class)
        .hasMessageContaining("Server unavailable");
  }

  @Test(expected = IllegalStateException.class)
  public void test_add_closed() throws Exception {
    closeContext();
    context.add(WeaviateObject.of(o -> o.properties(Map.of())));
  }

  @Test(expected = IllegalStateException.class)
  public void test_startAfterClose() throws Exception {
    closeContext();
    context.start();
  }

  /**
   * Read the next Data message from the stream and ACK it.
   * This method does not wait for the server to process the Acks.
   */
  private List<String> recvDataAndAck() throws InterruptedException {
    List<String> received = recvData();
    out.emitEventAsync(new Event.Acks(received));
    return received;
  }

  /** Read the next Data message from the stream. */
  private List<String> recvData() throws InterruptedException {
    WeaviateProtoBatch.BatchStreamRequest.Data data = in.expectMessage(DATA).getData();
    return Stream.concat(
        data.getObjects().getValuesList().stream().map(WeaviateProtoBatch.BatchObject::getUuid),
        data.getReferences().getValuesList().stream().map(BatchContextTest::getBeacon))
        .toList();
  }

  static String getBeacon(WeaviateProtoBatch.BatchReference reference) {
    return "weaviate://localhost/" + reference.getToCollection() + "/" + reference.getToUuid();
  }

  /** OutboundStream is a mock which dispatches server-side events. */
  private static final class OutboundStream {
    private final StreamObserver<Event> stream;
    private final Executor eventThread;

    OutboundStream(StreamObserver<Event> stream, Executor eventThread) {
      this.stream = stream;
      this.eventThread = eventThread;
    }

    /** Emit event on the current thread. */
    void emitEvent(Event event) {
      assert event != Event.EOF : "must not use synthetic EOF event";
      assert !(event instanceof Event.StreamHangup) : "must not use synthetic StreamHangup event";

      stream.onNext(event);
    }

    /**
     * Emit event on the {@link #eventThread}.
     */
    void emitEventAsync(Event event) {
      assert event != Event.EOF : "must not use synthetic EOF event";
      assert !(event instanceof Event.StreamHangup) : "must not use synthetic StreamHangup event";

      CompletableFuture.runAsync(() -> stream.onNext(event), eventThread);
    }

    /**
     * Terminate the server half of the stream abruptly.
     */

    void hangup() {
      log.debug("Hang up server half of the stream");

      CompletableFuture.runAsync(() -> stream.onError(new RuntimeException("whaam!")), eventThread);
    }

    /**
     * Close server half of the stream.
     *
     * @param ok Whether to emit {@link #pendingEvents}. Should only be false when
     *           stream is being hungup by the client, not by us.
     */
    CompletableFuture<Void> eof(boolean ok) {
      return CompletableFuture.runAsync(stream::onCompleted, eventThread);
    }
  }

  /** InboundStream is a spy which collects incoming messages in a queue. */
  private static final class InboundStream implements StreamObserver<Message> {
    /**
     * Block until the next message arrives on the stream.
     * When it does, assert it's of the expected type.
     *
     * @see WeaviateProtoBatch.BatchStreamRequest.MessageCase
     */
    WeaviateProtoBatch.BatchStreamRequest expectMessage(
        WeaviateProtoBatch.BatchStreamRequest.MessageCase messageCase) throws InterruptedException {
      WeaviateProtoBatch.BatchStreamRequest actual = stream.take();
      Assertions.assertThat(actual)
          .extracting(WeaviateProtoBatch.BatchStreamRequest::getMessageCase)
          .isEqualTo(messageCase);
      return actual;
    }

    final CompletableFuture<?> done = new CompletableFuture<>();
    private final BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> stream;
    private final OutboundStream outbound;

    InboundStream(BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> stream, OutboundStream outbound) {
      this.stream = stream;
      this.outbound = outbound;
    }

    @Override
    public void onCompleted() {
      done.complete(null);
    }

    @Override
    public void onError(Throwable t) {
      done.completeExceptionally(t);
      outbound.eof(false);
    }

    @Override
    public void onNext(Message message) {
      WeaviateProtoBatch.BatchStreamRequest req = asRequest(message);
      try {
        log.debug("Incoming message: {}", req.getMessageCase());

        boolean accepted = stream.offer(req, 5, TimeUnit.SECONDS);
        assert accepted : "message %s delivered before %s was consumed".formatted(
            req.getMessageCase(), stream.peek().getMessageCase());
      } catch (InterruptedException e) {
        log.debug("Interrupted");
        Thread.currentThread().interrupt();
      }
    }

    private static WeaviateProtoBatch.BatchStreamRequest asRequest(Message message) {
      var builder = WeaviateProtoBatch.BatchStreamRequest.newBuilder();
      message.appendTo(builder);
      return builder.build();
    }
  }
}
