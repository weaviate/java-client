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
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class BatchContextTest {
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

  private CompletableStreamFactory factory;
  private MockServerStream stream;
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
        .build();
    context.start();
    stream = factory.serverStream.get();
  }

  @After
  public void closeContext() throws IOException {
    if (context != null) {
      // Some of the tests may close the context, so this
      // implicitly tests that closing it multiple times is OK.
      context.close();
      context = null;
    }
    stream = null;
    factory = null;
  }

  @Test
  public void test_sendOneBatch() throws Exception {
    expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    stream.emitEvent(Event.STARTED);

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

    stream.emitEvent(new Event.Results(received, Collections.emptyMap()));

    // Since MockServerStream runs in the same thread as this test,
    // the context will be updated before the last emitEvent returns.
    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_drainOnClose() throws Exception {
    expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    stream.emitEvent(Event.STARTED);

    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 2; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    // Contrary the test above, we expect the objects to be sent
    // only after context.close(), as the half-empty batch will
    // be drained. Similarly, we want to ack everything as it arrives.
    ExecutorService exec = Executors.newSingleThreadExecutor();
    Future<?> mockServer = exec.submit(() -> {
      try {
        List<String> received = ack();
        Assertions.assertThat(tasks)
            .extracting(TaskHandle::id).containsExactlyInAnyOrderElementsOf(received);
        Assertions.assertThat(tasks)
            .extracting(TaskHandle::isAcked).allMatch(CompletableFuture::isDone);

        stream.emitEvent(new Event.Results(received, Collections.emptyMap()));
      } catch (InterruptedException e) {
        throw new RuntimeException("mock server interrupted", e);
      }
    });

    context.close();
    mockServer.get(); // Wait for the "mock server" to process the data message.

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_backoff() throws Exception {
    expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    stream.emitEvent(Event.STARTED);

    stream.emitEvent(new Event.Backoff(BATCH_SIZE / 2));

    List<TaskHandle> tasks = new ArrayList<>();
    ExecutorService exec = Executors.newSingleThreadExecutor();
    Future<?> testUser = exec.submit(() -> {
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
    stream.emitEvent(new Event.Results(received, Collections.emptyMap()));

    testUser.get(); // Finish populating batch context.

    // Since testUser will try and add BATCH_SIZE no. objects,
    // we should expect there to be exactly 2 batches.
    received = ack();
    Assertions.assertThat(received).hasSize(BATCH_SIZE / 2);
    stream.emitEvent(new Event.Results(received, Collections.emptyMap()));

    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test
  public void test_backoffBacklog() throws Exception {
    expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    stream.emitEvent(Event.STARTED);

    // Pre-fill the batch without triggering a flush (n-1).
    List<TaskHandle> tasks = new ArrayList<>();
    for (int i = 0; i < BATCH_SIZE - 1; i++) {
      tasks.add(context.add(WeaviateObject.of()));
    }

    int batchSizeNew = BATCH_SIZE / 2;

    // Force the last BATCH_SIZE / 2 - 1 items to be transferred to the backlog.
    stream.emitEvent(new Event.Backoff(batchSizeNew));

    // The next item will go on the backlog and the trigger a flush,
    // which will continue to send batches and re-populate the from
    // the backlog as long as the batch is full, so we should expect
    // to see 2 batches of size BATCH_SIZE / 2 each.
    List<String> received;
    tasks.add(context.add(WeaviateObject.of()));

    Assertions.assertThat(received = ack()).hasSize(batchSizeNew);
    stream.emitEvent(new Event.Results(received, Collections.emptyMap()));

    Assertions.assertThat(received = ack()).hasSize(batchSizeNew);
    stream.emitEvent(new Event.Results(received, Collections.emptyMap()));

    context.close();

    Assertions.assertThat(tasks).extracting(TaskHandle::result)
        .allMatch(CompletableFuture::isDone)
        .extracting(CompletableFuture::get).extracting(TaskHandle.Result::error)
        .allMatch(Optional::isEmpty);
  }

  @Test(expected = IllegalStateException.class)
  public void test_add_closed() throws Exception {
    expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.START);
    context.close();
    context.add(WeaviateObject.of(o -> o.properties(Map.of())));
  }

  /** Consume the next message and assert its type. */
  private WeaviateProtoBatch.BatchStreamRequest expectMessage(
      WeaviateProtoBatch.BatchStreamRequest.MessageCase messageCase) throws InterruptedException {
    WeaviateProtoBatch.BatchStreamRequest actual = stream.recv();
    Assertions.assertThat(actual)
        .extracting(WeaviateProtoBatch.BatchStreamRequest::getMessageCase)
        .isEqualTo(messageCase);
    return actual;
  }

  private List<String> ack() throws InterruptedException {
    WeaviateProtoBatch.BatchStreamRequest req = expectMessage(WeaviateProtoBatch.BatchStreamRequest.MessageCase.DATA);
    WeaviateProtoBatch.BatchStreamRequest.Data data = req.getData();
    List<String> ids = Stream.concat(
        data.getObjects().getValuesList().stream().map(WeaviateProtoBatch.BatchObject::getUuid),
        data.getReferences().getValuesList().stream().map(MockServerStream::getBeacon))
        .toList();
    stream.emitEvent(new Event.Acks(ids));
    return ids;
  }

  private class CompletableStreamFactory implements StreamFactory<Message, Event> {
    final CompletableFuture<MockServerStream> serverStream = new CompletableFuture<>();

    @Override
    public StreamObserver<Message> createStream(StreamObserver<Event> recv) {
      MockServerStream mock = new MockServerStream(recv);
      serverStream.complete(mock);
      return mock;
    }
  }

  private class MockServerStream implements StreamObserver<Message> {
    private final BlockingQueue<WeaviateProtoBatch.BatchStreamRequest> requestQueue;
    private final CompletableFuture<?> done;
    private final StreamObserver<Event> eventStream;

    public MockServerStream(StreamObserver<Event> eventStream) {
      this.eventStream = requireNonNull(eventStream, "eventStream is null");
      this.requestQueue = new ArrayBlockingQueue<WeaviateProtoBatch.BatchStreamRequest>(1);
      this.done = new CompletableFuture<>();
    }

    WeaviateProtoBatch.BatchStreamRequest recv() throws InterruptedException {
      return requestQueue.take();
    }

    void emitEvent(Event event) {
      assert event != Event.EOF : "server-side stream must be closed automatically";
      if (event instanceof Event.StreamHangup hangup) {
        eventStream.onError(hangup.exception());
      } else {
        eventStream.onNext(event);
      }
    }

    void hangupStream() {
      emitEvent(new Event.StreamHangup(new RuntimeException("whaam!")));
    }

    private void closeStream(Object result, Throwable ex) {
      eventStream.onCompleted();
    }

    @Override
    public void onCompleted() {
      done.complete(null);
      eventStream.onCompleted();
    }

    @Override
    public void onError(Throwable t) {
      done.completeExceptionally(t);
      eventStream.onCompleted();
    }

    @Override
    public void onNext(Message message) {
      requestQueue.offer(asRequest(message));
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
