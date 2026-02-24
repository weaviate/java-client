package io.weaviate.client6.v1.api.collections.batch;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Before;
import org.junit.Test;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

public class TranslatingStreamFactoryTest {
  private static final StreamObserver<Event> NOP_RECV = new StreamObserver<Event>() {

    @Override
    public void onCompleted() {
      throw new UnsupportedOperationException("Unimplemented method 'onCompleted'");
    }

    @Override
    public void onError(Throwable t) {
      throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    @Override
    public void onNext(Event reply) {
      throw new UnsupportedOperationException("Unimplemented method 'onNext'");
    }
  };

  private static final SpyStreamObserver<WeaviateProtoBatch.BatchStreamRequest> SPY_SEND;
  private static final SpyStreamObserver<Event> SPY_RECV;

  // RECV cannot be final, because we will only assign it when createStream
  // is called in the static constructor.
  private static StreamObserver<WeaviateProtoBatch.BatchStreamReply> RECV;
  private static final StreamObserver<Message> SEND;

  static {
    SPY_SEND = new SpyStreamObserver<>();
    SPY_RECV = new SpyStreamObserver<>();

    SEND = new TranslatingStreamFactory(recv -> {
      RECV = recv; // capture the underlying observer
      return SPY_SEND;
    }).createStream(SPY_RECV);
  }

  @Before
  public void resetSpies() {
    SPY_SEND.reset();
    SPY_RECV.reset();
  }

  @Test
  public void testMessenger_onNext_start() {
    SEND.onNext(Message.start(Optional.of(ConsistencyLevel.ONE)));

    Assertions.assertThat(SPY_SEND.getLast())
        .asInstanceOf(InstanceOfAssertFactories.type(WeaviateProtoBatch.BatchStreamRequest.class))
        .extracting(WeaviateProtoBatch.BatchStreamRequest::getStart).as("start").isNotNull()
        .extracting(WeaviateProtoBatch.BatchStreamRequest.Start::getConsistencyLevel).as("consistency level")
        .isEqualTo(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE);
  }

  @Test
  public void testMessenger_onNext_stop() {
    SEND.onNext(Message.stop());

    Assertions.assertThat(SPY_SEND.getLast())
        .asInstanceOf(InstanceOfAssertFactories.type(WeaviateProtoBatch.BatchStreamRequest.class))
        .extracting(WeaviateProtoBatch.BatchStreamRequest::getStop).as("stop").isNotNull();
  }

  @Test
  public void testMessenger_onNext_data() {
    // This message sets BatchStreamRequest.Data so it's not null.
    Message message = builder -> builder
        .setData(WeaviateProtoBatch.BatchStreamRequest.Data.newBuilder().build());
    SEND.onNext(message);

    Assertions.assertThat(SPY_SEND.getLast())
        .asInstanceOf(InstanceOfAssertFactories.type(WeaviateProtoBatch.BatchStreamRequest.class))
        .extracting(WeaviateProtoBatch.BatchStreamRequest::getData).as("data").isNotNull();
  }

  @Test
  public void testMessenger_onError() {
    Throwable whaam = new Exception("Whaam!");
    SEND.onError(whaam);
    Assertions.assertThat(SPY_SEND.getLast()).isEqualTo(whaam);
  }

  @Test
  public void testMessenger_onCompleted() {
    SEND.onCompleted();
    Assertions.assertThat(SPY_SEND.getLast()).isEqualTo(SpyStreamObserver.COMPLETED);
  }

  @Test
  public void testEventer_onNext_started() {
    RECV.onNext(WeaviateProtoBatch.BatchStreamReply.newBuilder()
        .setStarted(WeaviateProtoBatch.BatchStreamReply.Started.newBuilder()).build());

    Assertions.assertThat(SPY_RECV.getLast()).isEqualTo(Event.STARTED);
  }

  @Test
  public void testEventer_onNext_shuttingDown() {
    RECV.onNext(WeaviateProtoBatch.BatchStreamReply.newBuilder()
        .setShuttingDown(WeaviateProtoBatch.BatchStreamReply.ShuttingDown.newBuilder()).build());

    Assertions.assertThat(SPY_RECV.getLast()).isEqualTo(Event.SHUTTING_DOWN);
  }

  @Test
  public void testEventer_onNext_oom() {
    // TODO(dyma): update to read seconds
    RECV.onNext(WeaviateProtoBatch.BatchStreamReply.newBuilder()
        .setOutOfMemory(WeaviateProtoBatch.BatchStreamReply.OutOfMemory.newBuilder()).build());

    Assertions.assertThat(SPY_RECV.getLast())
        .asInstanceOf(InstanceOfAssertFactories.type(Event.Oom.class))
        .returns(300, Event.Oom::delaySeconds);
  }

  @Test
  public void testEventer_onNext_backoff() {
    RECV.onNext(WeaviateProtoBatch.BatchStreamReply.newBuilder()
        .setBackoff(WeaviateProtoBatch.BatchStreamReply.Backoff.newBuilder()
            .setBatchSize(92))
        .build());

    Assertions.assertThat(SPY_RECV.getLast())
        .asInstanceOf(InstanceOfAssertFactories.type(Event.Backoff.class))
        .returns(92, Event.Backoff::maxSize);
  }

  @Test
  public void testEventer_onNext_acks() {
    RECV.onNext(WeaviateProtoBatch.BatchStreamReply.newBuilder()
        .setAcks(WeaviateProtoBatch.BatchStreamReply.Acks.newBuilder()
            .addAllUuids(List.of("uuid-1", "uuid-2"))
            .addAllBeacons(List.of("beacon-1", "beacon-2")))
        .build());

    Assertions.assertThat(SPY_RECV.getLast())
        .asInstanceOf(InstanceOfAssertFactories.type(Event.Acks.class))
        .extracting(Event.Acks::acked)
        .asInstanceOf(InstanceOfAssertFactories.list(String.class))
        .containsOnly("uuid-1", "uuid-2", "beacon-1", "beacon-2");
  }

  @Test
  public void testEventer_onNext_results() {
    RECV.onNext(WeaviateProtoBatch.BatchStreamReply.newBuilder()
        .setResults(WeaviateProtoBatch.BatchStreamReply.Results.newBuilder()
            .addSuccesses(WeaviateProtoBatch.BatchStreamReply.Results.Success.newBuilder().setUuid("uuid-1"))
            .addSuccesses(WeaviateProtoBatch.BatchStreamReply.Results.Success.newBuilder().setUuid("beacon-1"))
            .addErrors(WeaviateProtoBatch.BatchStreamReply.Results.Error.newBuilder()
                .setUuid("uuid-2").setError("bad uuid!"))
            .addErrors(WeaviateProtoBatch.BatchStreamReply.Results.Error.newBuilder()
                .setBeacon("beacon-2").setError("bad beacon!")))
        .build());

    Event.Results results = Assertions.assertThat(SPY_RECV.getLast())
        .asInstanceOf(InstanceOfAssertFactories.type(Event.Results.class))
        .actual();

    Assertions.assertThat(results)
        .extracting(Event.Results::successful, InstanceOfAssertFactories.list(String.class))
        .containsOnly("uuid-1", "beacon-1");

    Assertions.assertThat(results)
        .extracting(Event.Results::errors, InstanceOfAssertFactories.map(String.class, String.class))
        .containsKeys("uuid-2", "beacon-2")
        .containsValues("bad uuid!", "bad beacon!");
  }

  @Test(expected = ProtocolViolationException.class)
  public void testEventer_onNext_notSet() {
    RECV.onNext(WeaviateProtoBatch.BatchStreamReply.newBuilder().build());
  }

  @Test
  public void testEventer_onError() {
    Throwable whaam = new Exception("Whaam!");
    RECV.onError(whaam);
    Assertions.assertThat(SPY_RECV.getLast()).isEqualTo(whaam);
  }

  @Test
  public void testEventer_onCompleted() {
    RECV.onCompleted();
    Assertions.assertThat(SPY_RECV.getLast()).isEqualTo(SpyStreamObserver.COMPLETED);
  }

  /**
   * Test utility that recods the value that the last callback was called with.
   */
  private static class SpyStreamObserver<T> implements StreamObserver<T> {
    /** Well-known value for {@link #onCompleted} callback. */
    static final Object COMPLETED = new Object();

    // Latest message delivered to this observer.
    private Object last;

    Object getLast() {
      return last;
    }

    void reset() {
      last = null;
    }

    @Override
    public void onNext(T req) {
      last = req;
    }

    @Override
    public void onError(Throwable t) {
      last = t;
    }

    @Override
    public void onCompleted() {
      last = COMPLETED;
    }

  }
}
