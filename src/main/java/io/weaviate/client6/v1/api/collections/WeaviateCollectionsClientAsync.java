package io.weaviate.client6.v1.api.collections;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateCollectionsClientAsync {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateCollectionsClientAsync(RestTransport restTransport, GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  public CollectionHandle<Map<String, Object>> use(String collectionName) {
    return new CollectionHandle<>(restTransport, grpcTransport,
        CollectionDescriptor.ofMap(collectionName));
  }

  public CompletableFuture<WeaviateCollection> create(String name) {
    return create(WeaviateCollection.of(name));
  }

  public CompletableFuture<WeaviateCollection> create(String name,
      Function<WeaviateCollection.Builder, ObjectBuilder<WeaviateCollection>> fn) {
    return create(WeaviateCollection.of(name, fn));
  }

  public CompletableFuture<WeaviateCollection> create(WeaviateCollection collection) {
    return this.restTransport.performRequestAsync(new CreateCollectionRequest(collection),
        CreateCollectionRequest._ENDPOINT);
  }

  public CompletableFuture<Optional<WeaviateCollection>> getConfig(String name) {
    return this.restTransport.performRequestAsync(new GetConfigRequest(name), GetConfigRequest._ENDPOINT);
  }

  public CompletableFuture<Void> delete(String name) {
    return this.restTransport.performRequestAsync(new DeleteCollectionRequest(name), DeleteCollectionRequest._ENDPOINT);
  }
}
