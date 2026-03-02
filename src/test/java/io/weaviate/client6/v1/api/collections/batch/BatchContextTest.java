package io.weaviate.client6.v1.api.collections.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.concurrent.GuardedBy;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class BatchContextTest {
  private static final Thread TEST_THREAD = Thread.currentThread();

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
  private static final ExecutorService BACKGROUND = Executors.newSingleThreadExecutor();

  /**
   * Server-side events must be emitted from a dedicated thread, to avoid
   * deadlocks between the test code and the client side-effects we expect
   * to take place and await.
   */
  private static final ExecutorService EVENT_THREAD = Executors.newSingleThreadExecutor();

  /**
   * This test models the client-side part of the stream as a blocking queue.
   * All streams created by {@link #createStream} are backed by the same queue,
   * which "survives" reconnects. The test code can listen on the same stream
   * even if the batch client re-creates it.
   */
  private static final BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> REQUEST_QUEUE = new ArrayBlockingQueue<>(
      1);

  /** Batch context for the current test case. */
  @GuardedBy("this")
  private BatchContext<Map<String, Object>> context;
  /** Server half of the stream. */
  private volatile OutboundStream out;
  /** Client half of the stream. */
  private volatile InboundStream in;

  private StreamObserver<Message> createStream(StreamObserver<Event> recv) {
    out = new OutboundStream(recv);
    in = new InboundStream(REQUEST_QUEUE, out);
    return in;
  }

  /**
   * Create new unstarted context with default maxSizeBytes, collection
   * descriptor, and collection handle defaults.
   */
  @Before
  public synchronized void startContext() {
    assert context == null;
    context = new BatchContext.Builder<>(this::createStream, MAX_SIZE_BYTES, DESCRIPTOR, DEFAULTS)
        .batchSize(BATCH_SIZE)
        .queueSize(QUEUE_SIZE)
        .maxReconnectRetries(MAX_RECONNECT_RETRIES)
        .build();
    context.start();
  }

  @After
  public synchronized void reset() throws Exception {
    if (context != null) {
      // Some of the tests may close the context, so this
      // implicitly tests that closing it multiple times is OK.
      context.close();
      context = null;
    }
    in = null;
    out = null;
    REQUEST_QUEUE.clear();
  }

  @AfterClass
  public static void shutdownExecutors() throws Exception {
    boolean terminated;

    BACKGROUND.shutdown();
    terminated = BACKGROUND.awaitTermination(100, TimeUnit.MILLISECONDS);
    assert terminated : "EXEC did not terminate after 5s";

    EVENT_THREAD.shutdown();
    terminated = EVENT_THREAD.awaitTermination(100, TimeUnit.MILLISECONDS);
    assert terminated : "EXEC did not terminate after 5s";
  }

  private static final WeaviateProtoBatch.BatchStreamRequest.MessageCase START = WeaviateProtoBatch.BatchStreamRequest.MessageCase.START;
  private static final WeaviateProtoBatch.BatchStreamRequest.MessageCase STOP = WeaviateProtoBatch.BatchStreamRequest.MessageCase.STOP;
  private static final WeaviateProtoBatch.BatchStreamRequest.MessageCase DATA = WeaviateProtoBatch.BatchStreamRequest.MessageCase.DATA;

  @Test
  public synchronized void test_sendOneBatch() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // BatchContext should flush the current batch once it hits its limit.
    // We will ack all items in the batch and send successful result for each one.
    List<String> received = recvDataAndAck();
    Assertions.assertThat(tasks)
        .extracting(TaskHandle::id).containsExactlyInAnyOrderElementsOf(received);
    Assertions.assertThat(tasks)
        .extracting(TaskHandle::isAcked).allMatch(CompletableFuture::isDone);

    out.beforeEof(new Event.Results(received, Collections.emptyMap()));

    // Since MockServer runs in the same thread as this test,
    // the context will be updated before the last emitEvent returns.
    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public synchronized void test_drainOnClose() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 2; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Contrary the test above, we expect the objects to be sent
    // only after context.close(), as the half-empty batch will
    // be drained. Similarly, we want to ack everything as it arrives.
    BACKGROUND.submit(() -> {
      try {
        List<String> received = recvDataAndAck();
        Assertions.assertThat(tasks).extracting(TaskHandle::id)
            .containsExactlyInAnyOrderElementsOf(received);
        Assertions.assertThat(tasks).extracting(TaskHandle::isAcked)
            .allMatch(CompletableFuture::isDone);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    out.beforeEof(new Event.Results(submitted, Collections.emptyMap()));

    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public synchronized void test_backoff() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    out.emitEvent(new Event.Backoff(BATCH_SIZE / 2));

    List<TaskHandle> tasks = new ArrayList<>();
    Future<?> backgroundAdd = BACKGROUND.submit(() -> {
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
    out.beforeEof(new Event.Results(received, Collections.emptyMap()));

    backgroundAdd.get(); // Finish populating batch context.

    // Since testUser will try and add BATCH_SIZE no. objects,
    // we should expect there to be exactly 2 batches.
    received = recvDataAndAck();
    Assertions.assertThat(received).hasSize(BATCH_SIZE / 2);
    out.beforeEof(new Event.Results(received, Collections.emptyMap()));

    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public synchronized void test_backoffBacklog() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    // Pre-fill the batch without triggering a flush (n-1).
    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    int batchSizeNew = BATCH_SIZE / 2;

    // Force the last BATCH_SIZE / 2 - 1 items to be transferred to the backlog.
    out.emitEvent(new Event.Backoff(batchSizeNew));

    // The next item will go on the backlog and the trigger a flush,
    // which will continue to send batches and re-populate from
    // the backlog as long as the batch is full, so we should expect
    // to see 2 batches of size BATCH_SIZE / 2 each.
    List<String> received;
    tasks.add(context.add(WeaviateObject.of()));

    Assertions.assertThat(received = recvDataAndAck()).hasSize(batchSizeNew);
    out.beforeEof(new Event.Results(received, Collections.emptyMap()));

    Assertions.assertThat(received = recvDataAndAck()).hasSize(batchSizeNew);
    out.beforeEof(new Event.Results(received, Collections.emptyMap()));

    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public synchronized void test_reconnect_onShutdown() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    out.emitEvent(Event.SHUTTING_DOWN);
    in.expectMessage(STOP);
    in.expectMessage(START);

    // Not strictly necessary -- we can close the context
    // before a new connection is established.
    out.emitEvent(Event.STARTED);
  }

  @Test
  public synchronized void test_reconnect_onOom() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();

    // OOM is the opposite of Ack -- trigger a flush first.
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Respond with OOM and wait for the client to close its end of the stream.
    in.expectMessage(DATA);
    out.emitEvent(new Event.Oom(0));

    // Close the server's end of the stream.
    in.expectMessage(STOP);

    // Allow the client to reconnect to another "instance" and Ack the batch.
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);
    recvDataAndAck();

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    out.beforeEof(new Event.Results(submitted, Collections.emptyMap()));
  }

  @Test
  public synchronized void test_reconnect_onStreamHangup() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();
    // Trigger a flush.
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Expect a new batch to arrive. Hangup the stream before sending the Acks.
    in.expectMessage(DATA);
    System.out.println("hangup the first time");
    out.hangup();

    // The client should try to reconnect, because the context is still open.
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    // The previous batch hasn't been acked, so we should expect to receive it
    // again.
    recvDataAndAck();

    // After the last batch has been acked, the Sender waits to take the next
    // item from the queue. Hangup the stream again, and add put another object
    // in the queue to wake the sender up.
    out.hangup();
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);
    tasks.add(context.add(WeaviateObject.of()));
    recvDataAndAck();

    // Now fill up the rest of the batch to trigger a flush. Ack the incoming batch.
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }
    recvDataAndAck();

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    out.beforeEof(new Event.Results(submitted, Collections.emptyMap()));
  }

  @Test
  public synchronized void test_reconnect_DrainAfterStreamHangup() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

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
    out.emitEvent(Event.STARTED);
    Future<?> backgroundAcks = BACKGROUND.submit(() -> {
      try {
        recvDataAndAck();
        recvDataAndAck();
        recvDataAndAck();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    List<String> submitted = tasks.stream().map(TaskHandle::id).toList();
    out.beforeEof(new Event.Results(submitted, Collections.emptyMap()));

    context.close();
    backgroundAcks.get();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public synchronized void test_closeAfterStreamHangup() throws Exception {
    in.expectMessage(START);
    out.emitEvent(Event.STARTED);

    out.hangup();
  }

  @Test
  public synchronized void test_maxReconnectRetries() throws Exception {
    in.expectMessage(START);

    // Drop the connection several times until the client
    // exhausts its reconnect attempts.
    int retries = 0;
    while (retries < MAX_RECONNECT_RETRIES) {
      out.hangup();
      in.expectMessage(START);
      retries++;
    }

    out.hangup();
    Assertions.assertThat(in.done).completesExceptionallyWithin(100, TimeUnit.MILLISECONDS);

    Assertions.assertThatThrownBy(() -> context.close())
        .isInstanceOf(IOException.class)
        .hasMessageContaining("Server unavailable");

    // Cleanup: unset the context to prevent test teardown code
    // from tripping on it while trying to close the context.
    context = null;
  }

  @Test(expected = IllegalStateException.class)
  public synchronized void test_add_closed() throws Exception {
    in.expectMessage(START);
    context.close();
    context.add(WeaviateObject.of(o -> o.properties(Map.of())));
  }

  @Test(expected = IllegalStateException.class)
  public synchronized void test_startAfterClose() throws Exception {
    in.expectMessage(START);
    context.close();
    context.start();
  }

  /**
   * Read the next Data message from the stream and ACK it.
   * This method does not wait for the server to process the Acks.
   */
  private List<String> recvDataAndAck() throws InterruptedException {
    List<String> received = recvData();
    out.emitEvent(new Event.Acks(received));
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
    private List<Event> beforeEof = new ArrayList<>();

    OutboundStream(StreamObserver<Event> stream) {
      this.stream = stream;
    }

    Future<?> emitEvent(Event event) {
      assert event != Event.EOF : "must not use synthetic EOF event";
      assert !(event instanceof Event.StreamHangup) : "must not use synthetic StreamHangup event";

      return EVENT_THREAD.submit(() -> stream.onNext(event));
    }

    /** Terminate the server half of the stream abruptly. */

    Future<?> hangup() {
      return EVENT_THREAD.submit(() -> stream.onError(new RuntimeException("whaam!")));
    }

    /** Emit events before closing the server half of the stream. */
    void beforeEof(Event... events) {
      this.beforeEof.addAll(Arrays.asList(events));
    }

    void eof(boolean ok) {
      assert Thread.currentThread() != TEST_THREAD : "test MUST NOT close/terminate the the stream";
      if (ok) {
        beforeEof.forEach(this::emitEvent);
      }
      EVENT_THREAD.submit(stream::onCompleted);
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
      outbound.eof(true);
    }

    @Override
    public void onError(Throwable t) {
      done.completeExceptionally(t);
      outbound.eof(false);
    }

    @Override
    public void onNext(Message message) {
      WeaviateProtoBatch.BatchStreamRequest req = asRequest(message);
      boolean accepted = stream.offer(req);
      assert accepted : "message %s delivered before %q was consumed".formatted(
          req.getMessageCase(), stream.peek().getMessageCase());
    }

    private static WeaviateProtoBatch.BatchStreamRequest asRequest(Message message) {
      var builder = WeaviateProtoBatch.BatchStreamRequest.newBuilder();
      message.appendTo(builder);
      return builder.build();
    }
  }
}
