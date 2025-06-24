package io.weaviate.client6.v1.api.collections.config;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.WeaviateCollection;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateConfigClientAsync {
  private final RestTransport restTransport;
  private final WeaviateCollectionsClientAsync collectionsClient;

  protected final CollectionDescriptor<?> collectionDescriptor;

  public WeaviateConfigClientAsync(CollectionDescriptor<?> collection, RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.collectionsClient = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);

    this.collectionDescriptor = collection;
  }

  public CompletableFuture<Optional<WeaviateCollection>> get() throws IOException {
    return collectionsClient.getConfig(collectionDescriptor.name());
  }

  public CompletableFuture<Void> addProperty(Property property) throws IOException {
    return this.restTransport.performRequestAsync(new AddPropertyRequest(collectionDescriptor.name(), property),
        AddPropertyRequest._ENDPOINT);
  }

  public CompletableFuture<Void> addReference(String name, String... dataTypes) throws IOException {
    return this.addProperty(Property.reference(name, dataTypes).toProperty());
  }

  public CompletableFuture<Void> update(String collectionName,
      Function<UpdateCollectionRequest.Builder, ObjectBuilder<UpdateCollectionRequest>> fn) throws IOException {
    return get().thenCompose(maybeCollection -> {
      var thisCollection = maybeCollection.orElseThrow();
      return this.restTransport.performRequestAsync(UpdateCollectionRequest.of(thisCollection, fn),
          UpdateCollectionRequest._ENDPOINT);
    });
  }
}
