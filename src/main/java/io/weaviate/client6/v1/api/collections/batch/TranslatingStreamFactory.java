package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamReply;

class TranslatingStreamFactory implements StreamFactory<StreamMessage, Event> {
  private final StreamFactory<WeaviateProtoBatch.BatchStreamRequest, WeaviateProtoBatch.BatchStreamReply> protoFactory;

  TranslatingStreamFactory(
      StreamFactory<WeaviateProtoBatch.BatchStreamRequest, WeaviateProtoBatch.BatchStreamReply> protoFactory) {
    this.protoFactory = requireNonNull(protoFactory, "protoFactory is null");
  }

  @Override
  public StreamObserver<StreamMessage> createStream(StreamObserver<Event> eventObserver) {
    return new MessageProducer(protoFactory.createStream(new EventHandler(eventObserver)));
  }

  private abstract class DelegatingStreamObserver<SourceT, TargetT> implements StreamObserver<TargetT> {
    protected final StreamObserver<SourceT> delegate;

    protected DelegatingStreamObserver(StreamObserver<SourceT> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void onCompleted() {
      delegate.onCompleted();
    }

    @Override
    public void onError(Throwable t) {
      delegate.onError(t);
    }
  }

  private final class MessageProducer
      extends DelegatingStreamObserver<WeaviateProtoBatch.BatchStreamRequest, StreamMessage> {
    private MessageProducer(StreamObserver<WeaviateProtoBatch.BatchStreamRequest> delegate) {
      super(delegate);
    }

    @Override
    public void onNext(StreamMessage message) {
      WeaviateProtoBatch.BatchStreamRequest.Builder builder = WeaviateProtoBatch.BatchStreamRequest.newBuilder();
      message.appendTo(builder);
      delegate.onNext(builder.build());
    }
  }

  private final class EventHandler extends DelegatingStreamObserver<Event, WeaviateProtoBatch.BatchStreamReply> {
    private EventHandler(StreamObserver<Event> delegate) {
      super(delegate);
    }

    @Override
    public void onNext(BatchStreamReply reply) {
      Event event = null;
      switch (reply.getMessageCase()) {
        case STARTED:
          event = Event.STARTED;
        case SHUTTING_DOWN:
          event = Event.SHUTTING_DOWN;
        case SHUTDOWN:
          event = Event.SHUTDOWN;
        case OUT_OF_MEMORY:
          event = Event.OOM;
        case BACKOFF:
          event = new Event.Backoff(reply.getBackoff().getBatchSize());
        case ACKS:
          Stream<String> uuids = reply.getAcks().getUuidsList().stream();
          Stream<String> beacons = reply.getAcks().getBeaconsList().stream();
          event = new Event.Acks(Stream.concat(uuids, beacons).toList());
        case RESULTS:
          List<String> successful = reply.getResults().getSuccessesList().stream()
              .map(detail -> {
                if (detail.hasUuid()) {
                  return detail.getUuid();
                } else if (detail.hasBeacon()) {
                  return detail.getBeacon();
                }
                throw new IllegalArgumentException("Result has neither UUID nor a beacon");
              })
              .toList();

          Map<String, String> errors = reply.getResults().getErrorsList().stream()
              .map(detail -> {
                String error = requireNonNull(detail.getError(), "error is null");
                if (detail.hasUuid()) {
                  return Map.entry(detail.getUuid(), error);
                } else if (detail.hasBeacon()) {
                  return Map.entry(detail.getBeacon(), error);
                }
                throw new IllegalArgumentException("Result has neither UUID nor a beacon");
              })
              .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
          event = new Event.Results(successful, errors);
        case MESSAGE_NOT_SET:
          throw new IllegalArgumentException("Message not set");
      }

      delegate.onNext(event);
    }
  }
}
