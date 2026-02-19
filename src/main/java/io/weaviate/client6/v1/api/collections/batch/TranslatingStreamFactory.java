package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamReply;

/**
 * TranslatingStreamFactory is an adaptor for the
 * {@link WeaviateGrpc.WeaviateStub#batchStream} factory. The returned stream
 * translates client-side messages into protobuf requests and server-side
 * replies into events.
 *
 * @see Message
 * @see Event
 */
class TranslatingStreamFactory implements StreamFactory<Message, Event> {
  private final StreamFactory<WeaviateProtoBatch.BatchStreamRequest, WeaviateProtoBatch.BatchStreamReply> protoFactory;

  TranslatingStreamFactory(
      StreamFactory<WeaviateProtoBatch.BatchStreamRequest, WeaviateProtoBatch.BatchStreamReply> protoFactory) {
    this.protoFactory = requireNonNull(protoFactory, "protoFactory is null");
  }

  @Override
  public StreamObserver<Message> createStream(StreamObserver<Event> recv) {
    return new Messenger(protoFactory.createStream(new Eventer(recv)));
  }

  /**
   * DelegatingStreamObserver delegates {@link #onCompleted} and {@link #onError}
   * to another observer and translates the messages in {@link #onNext}.
   *
   * @param <SourceT> the type of the incoming message.
   * @param <TargetT> the type of the message handed to the delegate.
   */
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

  /**
   * Messeger translates client's messages into batch stream requests.
   *
   * @see Message
   */
  private final class Messenger extends DelegatingStreamObserver<WeaviateProtoBatch.BatchStreamRequest, Message> {
    private Messenger(StreamObserver<WeaviateProtoBatch.BatchStreamRequest> delegate) {
      super(delegate);
    }

    @Override
    public void onNext(Message message) {
      WeaviateProtoBatch.BatchStreamRequest.Builder builder = WeaviateProtoBatch.BatchStreamRequest.newBuilder();
      message.appendTo(builder);
      delegate.onNext(builder.build());
    }
  }

  /**
   * Eventer translates server replies into events.
   *
   * @see Event
   */
  private final class Eventer extends DelegatingStreamObserver<Event, WeaviateProtoBatch.BatchStreamReply> {
    private Eventer(StreamObserver<Event> delegate) {
      super(delegate);
    }

    @Override
    public void onNext(BatchStreamReply reply) {
      Event event = null;
      switch (reply.getMessageCase()) {
        case STARTED:
          event = Event.STARTED;
          break;
        case SHUTTING_DOWN:
          event = Event.SHUTTING_DOWN;
          break;
        case SHUTDOWN:
          event = Event.EOF;
          break;
        case OUT_OF_MEMORY:
          // TODO(dyma): read this value from the message
          event = new Event.Oom(300);
          break;
        case BACKOFF:
          event = new Event.Backoff(reply.getBackoff().getBatchSize());
          break;
        case ACKS:
          Stream<String> uuids = reply.getAcks().getUuidsList().stream();
          Stream<String> beacons = reply.getAcks().getBeaconsList().stream();
          event = new Event.Acks(Stream.concat(uuids, beacons).toList());
          break;
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
          break;
        case MESSAGE_NOT_SET:
          throw new ProtocolViolationException("Message not set");
      }

      delegate.onNext(event);
    }
  }
}
