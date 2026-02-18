package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.OptionalInt;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.TransportOptions;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateBatchClient<PropertiesT> {
  private final CollectionHandleDefaults defaults;
  private final CollectionDescriptor<PropertiesT> collectionDescriptor;
  private final GrpcTransport grpcTransport;

  public WeaviateBatchClient(
      GrpcTransport grpcTransport,
      CollectionDescriptor<PropertiesT> collectionDescriptor,
      CollectionHandleDefaults defaults) {
    this.defaults = requireNonNull(defaults, "defaults is null");
    this.collectionDescriptor = requireNonNull(collectionDescriptor, "collectionDescriptor is null");
    this.grpcTransport = requireNonNull(grpcTransport, "grpcTransport is null");
  }

  /** Copy constructor with new defaults. */
  public WeaviateBatchClient(WeaviateBatchClient<PropertiesT> c, CollectionHandleDefaults defaults) {
    this.defaults = requireNonNull(defaults, "defaults is null");
    this.collectionDescriptor = c.collectionDescriptor;
    this.grpcTransport = c.grpcTransport;
  }

  public BatchContext<PropertiesT> start() {
    OptionalInt maxSizeBytes = grpcTransport.maxMessageSizeBytes();
    if (maxSizeBytes.isEmpty()) {
      throw new IllegalStateException("Server must have grpcMaxMessageSize configured to use server-side batching");
    }

    StreamFactory<Message, Event> streamFactory = new TranslatingStreamFactory(grpcTransport::createStream);
    BatchContext<PropertiesT> context = new BatchContext<>(
        streamFactory,
        maxSizeBytes.getAsInt(),
        collectionDescriptor,
        defaults);

    if (isWeaviateCloudOnGoogleCloud(grpcTransport.host())) {
      context.scheduleReconnect(GCP_RECONNECT_INTERVAL_SECONDS);
    }

    return context;
  }

  private static final int GCP_RECONNECT_INTERVAL_SECONDS = 160;

  private static boolean isWeaviateCloudOnGoogleCloud(String host) {
    return TransportOptions.isWeaviateDomain(host) && TransportOptions.isGoogleCloudDomain(host);
  }
}
