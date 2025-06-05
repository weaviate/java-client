package io.weaviate.client6.v1.api.collections.config;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.WeaviateCollection;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateConfigClientAsync {
  private final RestTransport transport;
  private final WeaviateCollectionsClientAsync collectionsClient;

  protected final CollectionDescriptor<?> collection;

  public WeaviateConfigClientAsync(CollectionDescriptor<?> collection, RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.transport = restTransport;
    this.collectionsClient = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);

    this.collection = collection;
  }

  public CompletableFuture<Optional<WeaviateCollection>> get() throws IOException {
    return collectionsClient.getConfig(collection.name());
  }

  public CompletableFuture<Void> addProperty(Property property) throws IOException {
    return this.transport.performRequestAsync(new AddPropertyRequest(collection.name(), property),
        AddPropertyRequest._ENDPOINT);
  }

  public CompletableFuture<Void> addReference(String name, String... dataTypes) throws IOException {
    return this.addProperty(Property.reference(name, dataTypes).toProperty());
  }
}
