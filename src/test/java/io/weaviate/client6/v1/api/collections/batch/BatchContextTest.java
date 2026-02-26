package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
   * Dedicated executor for occasional asynchrony.
   *
   * <p>
   * Most of the time the "server", the "user", the "test" processes
   * will run on the main thread, one at a time, when the logic permits.
   * In some cases it is useful to put at least one of those processes
   * onto a separate thread. E.g. when the batch is being drained, but
   * the main thread is blocked by {@link BatchContext#close}.
   */
  private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

  /**
   * Maximum gRPC message size of 2KB .
   * 1KB is {@link MessageSizeUtil#SAFETY_MARGIN}.
   */
  private static final int MAX_SIZE_BYTES = 2 * 1024;
  private static final int BATCH_SIZE = 10;
  private static final int QUEUE_SIZE = 1;
  private static final int MAX_RECONNECT_RETRIES = 1;

  private CompletableStreamFactory factory;
  private MockServer server;
  private BatchContext<Map<String, Object>> context;

  /**
   * Create new unstarted context with default maxSizeBytes, collection
   * descriptor, and collection handle defaults.
   */
  @Before
  public void setupContext() throws InterruptedException, ExecutionException {
    factory = new CompletableStreamFactory();
    context = new BatchContext.Builder<>(factory, MAX_SIZE_BYTES, DESCRIPTOR, DEFAULTS)
        .batchSize(BATCH_SIZE)
        .queueSize(QUEUE_SIZE)
        .maxReconnectRetries(MAX_RECONNECT_RETRIES)
        .build();
    context.start();
    server = factory.serverStream.get();
  }

  @After
  public void closeContext() throws Exception {
    if (context != null) {
      // Some of the tests may close the context, so this
      // implicitly tests that closing it multiple times is OK.
      context.close();
      context = null;
    }
    if (factory != null) {
      factory.close();
      factory = null;
    }
    server = null;
  }

  @AfterClass
  public static void closeExecutor() throws Exception {
    EXEC.shutdown();
    boolean terminated = EXEC.awaitTermination(5, TimeUnit.SECONDS);
    assert terminated : "EXEC did not terminate after 5s";
  }

  @Test
  public void test_sendOneBatch() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // BatchContext should flush the current batch once it hits its limit.
    // We will ack all items in the batch and send successful result for each one.
    List<String> received = ack();
    Assertions.assertThat(tasks)
        .extracting(TaskHandle::id).containsExactlyInAnyOrderElementsOf(received);
    Assertions.assertThat(tasks)
        .extracting(TaskHandle::isAcked).allMatch(CompletableFuture::isDone);

    Future<?> results = server.emitEvent(new Event.Results(received, Collections.emptyMap()));

    // Since MockServer runs in the same thread as this test,
    // the context will be updated before the last emitEvent returns.
    context.close();

    results.get(); // Wait until the Results event has been processed.
    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_drainOnClose() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 2; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Contrary the test above, we expect the objects to be sent
    // only after context.close(), as the half-empty batch will
    // be drained. Similarly, we want to ack everything as it arrives.
    Future<?> backgroundAcks = EXEC.submit(() -> {
      try {
        List<String> received = ack();
        Assertions.assertThat(tasks)
            .extracting(TaskHandle::id).containsExactlyInAnyOrderElementsOf(received);
        Assertions.assertThat(tasks)
            .extracting(TaskHandle::isAcked).allMatch(CompletableFuture::isDone);

        // Wait until the Results event's been processed to guarantee
        // that the tasks' futures are completed before asserting.
        Future<?> results = server.emitEvent(new Event.Results(received, Collections.emptyMap()));
        results.get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    context.close();
    backgroundAcks.get(); // Wait for the "mock server" to process the data message.

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_backoff() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    server.emitEvent(new Event.Backoff(BATCH_SIZE / 2));

    List<TaskHandle> tasks = new ArrayList<>();
    Future<?> backgroundAdd = EXEC.submit(() -> {
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
    List<String> received = ack();
    Assertions.assertThat(received).hasSize(BATCH_SIZE / 2);
    server.emitEvent(new Event.Results(received, Collections.emptyMap()));

    backgroundAdd.get(); // Finish populating batch context.

    // Since testUser will try and add BATCH_SIZE no. objects,
    // we should expect there to be exactly 2 batches.
    received = ack();
    Assertions.assertThat(received).hasSize(BATCH_SIZE / 2);
    server.emitEvent(new Event.Results(received, Collections.emptyMap()));

    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_backoffBacklog() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    // Pre-fill the batch without triggering a flush (n-1).
    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    int batchSizeNew = BATCH_SIZE / 2;

    // Force the last BATCH_SIZE / 2 - 1 items to be transferred to the backlog.
    server.emitEvent(new Event.Backoff(batchSizeNew));

    // The next item will go on the backlog and the trigger a flush,
    // which will continue to send batches and re-populate from
    // the backlog as long as the batch is full, so we should expect
    // to see 2 batches of size BATCH_SIZE / 2 each.
    List<String> received;
    tasks.add(context.add(WeaviateObject.of()));

    Assertions.assertThat(received = ack()).hasSize(batchSizeNew);
    server.emitEvent(new Event.Results(received, Collections.emptyMap()));

    Assertions.assertThat(received = ack()).hasSize(batchSizeNew);
    server.emitEvent(new Event.Results(received, Collections.emptyMap()));

    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_reconnect_onShutdown() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    server.emitEvent(Event.SHUTTING_DOWN);
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.STOP);

    // stream.eof();
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);

    // Not strictly necessary -- we can close the context
    // before a new connection is established.
    server.emitEvent(Event.STARTED);
  }

  @Test
  public void test_reconnect_onOom() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    // OOM is the opposite of Ack -- trigger a flush first.
    for (int i = 0; i < BATCH_SIZE; i++) {
      context.add(WeaviateObject.of());
    }

    // Respond with OOM and wait for the client to close its end of the stream.
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.DATA);
    server.emitEvent(new Event.Oom(0));

    // Close the server's end of the stream.
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.STOP);

    // Allow the client to reconnect to another "instance" and Ack the batch.
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);
    ack();
  }

  @Test
  public void test_reconnect_onStreamHangup() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    // Trigger a flush.
    for (int i = 0; i < BATCH_SIZE; i++) {
      context.add(WeaviateObject.of());
    }

    // Expect a new batch to arrive. Hangup the stream before sending the Acks.
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.DATA);
    System.out.println("hangup the first time");
    server.hangup();

    // The client should try to reconnect, because the context is still open.
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    // The previous batch hasn't been acked, so we should expect to receive it
    // again.
    ack();

    // After the last batch has been acked, the Sender waits to take the next
    // item from the queue. Hangup the stream again, and add put another object
    // in the queue to wake the sender up.
    server.hangup();
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);
    context.add(WeaviateObject.of());
    ack();

    // Now fill up the rest of the batch to trigger a flush. Ack the incoming batch.
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      context.add(WeaviateObject.of());
    }
    ack();
  }

  @Test
  public void test_reconnect_DrainAfterStreamHangup() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();

    // Trigger a flush.
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Expect a new batch to arrive. Ack the batch, and trigger another flush.
    ack();
    for (int i = 0; i < BATCH_SIZE; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Ack the latest batch, but now Before sending back the results
    // for either one, hang up the stream.
    // On hangup, client should re-populate the batch from the WIP buffer.
    // Add one more item to overflow, so that there are now 3 pending batches.
    ack();
    server.hangup();
    tasks.add(context.add(WeaviateObject.of()));

    // The client will try to reconnect, because the context is still open.
    // When the server starts accepting connections again, the client should
    // drain the remaining BATCH_SIZE+1 objects as we close the context.
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);
    Future<?> backgroundAcks = EXEC.submit(() -> {
      try {
        List<String> ids = ack();
        server.emitEvent(new Event.Results(ids, Collections.emptyMap()));

        ids = ack();
        server.emitEvent(new Event.Results(ids, Collections.emptyMap()));

        ids = ack();
        Future<?> lastEvent = server.emitEvent(new Event.Results(ids, Collections.emptyMap()));
        lastEvent.get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    context.close();
    backgroundAcks.get();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_closeAfterStreamHangup() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    server.emitEvent(Event.STARTED);

    server.hangup();
  }

  @Test
  public void test_maxReconnectRetries() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);

    // Drop the connection several times until the client exhausts its reconnect attempts.
    int retries = 0;
    while (retries < MAX_RECONNECT_RETRIES) {
      server.hangup();
      server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
      retries++;
    }

    Future<?> fatalHangup = server.hangup();
    try {
      fatalHangup.get();
    } catch (InterruptedException ignored) {
      // BatchContext#shutdownNow might interrupt the parent thread.
    }

    Assertions.assertThatThrownBy(() -> context.close())
        .isInstanceOf(IOException.class)
        .hasMessageContaining("Server unavailable");

    // Cleanup: unset the context and factory to prevent test teardown code
    // from tripping on it while trying to close the context.
    context = null;

    try {
      factory.close();
    } catch (InterruptedException ignored) {
    } finally {
    factory = null;
    }
  }

  @Test(expected = IllegalStateException.class)
  public void test_add_closed() throws Exception {
    server.expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    context.close();
    context.add(WeaviateObject.of(o -> o.properties(Map.of())));
  }

  /**
   * Read the next Data message from the stream and ACK it.
   * This method does not wait for the server to process the Acks.
   */
  private List<String> ack() throws InterruptedException {
    WeaviateProtoBatch.BatchStreamRequest.Data data = server
        .expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.DATA)
        .getData();
    List<String> ids = Stream.concat(
        data.getObjects().getValuesList().stream().map(WeaviateProtoBatch.BatchObject::getUuid),
        data.getReferences().getValuesList().stream().map(MockServer::getBeacon))
        .toList();
    server.emitEvent(new Event.Acks(ids));
    return ids;
  }

  private static class CompletableStreamFactory implements StreamFactory<Message, Event>, AutoCloseable {
    /**
     * {@link MockServer} models the client-side part of the stream as a
     * blocking queue. Streams created by this factory are backed by the
     * same queue, which "survives" reconnects, meaning the test code can
     * listen on the same "stream" even if the batch client "recreates" it.
     */
    private final BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> backingQueue = new ArrayBlockingQueue<>(
        1);

    /**
     * Server-side events are emitted from a dedicated thread, to avoid deadlocks
     * between the test code and the client side effects it expects to take place.
     */
    private final ExecutorService eventThread = Executors.newSingleThreadExecutor();

    /** Server-side part of the stream. */
    final CompletableFuture<MockServer> serverStream = new CompletableFuture<>();

    @Override
    public StreamObserver<Message> createStream(StreamObserver<Event> recv) {
      MockServer stream = new MockServer(recv, eventThread, backingQueue);
      serverStream.complete(stream);
      return stream;
    }

    @Override
    public void close() throws Exception {
      eventThread.shutdown();
      boolean terminated = eventThread.awaitTermination(5, TimeUnit.SECONDS);
      assert terminated : "EVENT_THREAD did not terminate after 5s";
    }
  }

  private static class MockServer implements StreamObserver<Message> {
    /** Server-side part of the stream */
    private final StreamObserver<Event> eventStream;
    /**
     * Dedicated executor allows closing the server-side part of the stream
     * asynchronously to avoid running on-event hooks on the same thread
     * that uses client-side stream.
     */
    private final ExecutorService eventExecutor;

    private final BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> requestQueue;

    public MockServer(
        StreamObserver<Event> eventStream,
        ExecutorService eventExecutor,
        BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> requestQueue) {
      this.eventStream = requireNonNull(eventStream, "eventStream is null");
      this.requestQueue = requireNonNull(requestQueue, "requestQueue is null");
      this.eventExecutor = requireNonNull(eventExecutor, "eventExecutor is null");
    }

    Future<?> emitEvent(Event event) {
      if (event == Event.EOF) {
        assert Thread.currentThread() != TEST_THREAD : "test MUST NOT close/terminate the the stream";
      }

      return eventExecutor.submit(() -> {
        System.out.println("emit " + event);
        if (event == Event.EOF) {
          eventStream.onCompleted();
        } else if (event instanceof Event.StreamHangup hangup) {
          eventStream.onError(hangup.exception());
        } else {
          eventStream.onNext(event);
        }
        System.out.println(event + " processed!");
      });
    }

    /** Terminate the server-side of the stream abruptly. */
    Future<?> hangup() {
      return emitEvent(new Event.StreamHangup(new RuntimeException("whaam!")));
    }

    /**
     * Block until the next message arrives on the stream.
     * When it does, assert it's of the expected type.
     *
     * @see WeaviateProtoBatch.BatchStreamRequest.MessageCase
     */
    WeaviateProtoBatch.BatchStreamRequest expectMessage(
        WeaviateProtoBatch.BatchStreamRequest.MessageCase messageCase) throws InterruptedException {
      WeaviateProtoBatch.BatchStreamRequest actual = requestQueue.take();
      Assertions.assertThat(actual)
          .extracting(WeaviateProtoBatch.BatchStreamRequest::getMessageCase)
          .isEqualTo(messageCase);
      return actual;
    }

    @Override
    public void onCompleted() {
      emitEvent(Event.EOF);
    }

    @Override
    public void onError(Throwable t) {
      emitEvent(Event.EOF);
    }

    @Override
    public void onNext(Message message) {
      boolean accepted = requestQueue.offer(asRequest(message));
      assert accepted : "message rejected by the queue";
    }

    private static WeaviateProtoBatch.BatchStreamRequest asRequest(Message message) {
      var builder = WeaviateProtoBatch.BatchStreamRequest.newBuilder();
      message.appendTo(builder);
      return builder.build();
    }

    static String getBeacon(WeaviateProtoBatch.BatchReference reference) {
      return "weaviate://localhost/" + reference.getToCollection() + "/" + reference.getToUuid();
    }
  }
}
