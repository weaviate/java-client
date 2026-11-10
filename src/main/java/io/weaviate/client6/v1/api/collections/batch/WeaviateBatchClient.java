package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.OptionalInt;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TransportOptions;
import io.weaviate.client6.v1.internal.VersionSupport;
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

  public BatchContext<PropertiesT> start(
      Function<BatchContext.Builder<PropertiesT>, ObjectBuilder<BatchContext<PropertiesT>>> fn) {
    int maxSizeBytes = requireServerSideBatching();

    StreamFactory<Message, Event> streamFactory = new TranslatingStreamFactory(grpcTransport::createStream);
    BatchContext.Builder<PropertiesT> builder = new BatchContext.Builder<>(
        streamFactory, maxSizeBytes, collectionDescriptor, defaults);
    BatchContext<PropertiesT> context = fn.apply(builder).build();

    if (isWeaviateCloudOnGoogleCloud(grpcTransport.host())) {
      context.scheduleReconnect(GCP_RECONNECT_INTERVAL_SECONDS);
    }

    context.start();
    return context;
  }

  public BatchContext<PropertiesT> start() {
    int maxSizeBytes = requireServerSideBatching();

    StreamFactory<Message, Event> streamFactory = new TranslatingStreamFactory(grpcTransport::createStream);
    BatchContext<PropertiesT> context = new BatchContext.Builder<>(
        streamFactory,
        maxSizeBytes,
        collectionDescriptor,
        defaults).build();

    if (isWeaviateCloudOnGoogleCloud(grpcTransport.host())) {
      context.scheduleReconnect(GCP_RECONNECT_INTERVAL_SECONDS);
    }

    context.start();
    return context;
  }

  private int requireServerSideBatching() {
    grpcTransport.serverVersion().ifPresent(version -> {
      if (!VersionSupport.isAtLeast(version, VersionSupport.SERVER_SIDE_BATCHING_VERSION)) {
        throw new IllegalStateException(
            "Server-side batching requires Weaviate %s or newer, server version is %s"
                .formatted(VersionSupport.SERVER_SIDE_BATCHING_VERSION, version));
      }
    });

    OptionalInt maxSizeBytes = grpcTransport.maxMessageSizeBytes();
    if (maxSizeBytes.isEmpty()) {
      throw new IllegalStateException("Server must have grpcMaxMessageSize configured to use server-side batching");
    }
    return maxSizeBytes.getAsInt();
  }

  private static final int GCP_RECONNECT_INTERVAL_SECONDS = 160;

  private static boolean isWeaviateCloudOnGoogleCloud(String host) {
    return TransportOptions.isWeaviateDomain(host) && TransportOptions.isGoogleCloudDomain(host);
  }
}
