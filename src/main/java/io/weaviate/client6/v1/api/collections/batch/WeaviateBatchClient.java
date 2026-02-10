package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.OptionalInt;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;

public class WeaviateBatchClient<PropertiesT> {
  private final CollectionHandleDefaults defaults;
  private final GrpcTransport grpcTransport;

  public WeaviateBatchClient(GrpcTransport grpcTransport, CollectionHandleDefaults defaults) {
    this.defaults = requireNonNull(defaults, "defaults is null");
    this.grpcTransport = requireNonNull(grpcTransport, "grpcTransport is null");
  }

  /** Copy constructor with new defaults. */
  public WeaviateBatchClient(WeaviateBatchClient<PropertiesT> c, CollectionHandleDefaults defaults) {
    this.defaults = requireNonNull(defaults, "defaults is null");
    this.grpcTransport = c.grpcTransport;
  }

  public BatchContext<PropertiesT> start() {
    OptionalInt maxSizeBytes = grpcTransport.maxMessageSizeBytes();
    if (maxSizeBytes.isEmpty()) {
      throw new IllegalStateException("Server must have grpcMaxMessageSize configured to use server-side batching");
    }
    StreamFactory<Message, Event> streamFactory = new TranslatingStreamFactory(grpcTransport::createStream);
    return new BatchContext<>(streamFactory, maxSizeBytes.getAsInt(), defaults.consistencyLevel());
  }
}
