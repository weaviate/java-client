package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.Map;
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

  public CompletableFuture<WeaviateCollection> create(String name) throws IOException {
    return create(WeaviateCollection.of(name));
  }

  public CompletableFuture<WeaviateCollection> create(String name,
      Function<WeaviateCollection.Builder, ObjectBuilder<WeaviateCollection>> fn) throws IOException {
    return create(WeaviateCollection.of(name, fn));
  }

  public CompletableFuture<WeaviateCollection> create(WeaviateCollection collection) throws IOException {
    return this.restTransport.performRequestAsync(new CreateCollectionRequest(collection),
        CreateCollectionRequest._ENDPOINT);
  }

  public void delete(String name) throws IOException {
    this.restTransport.performRequestAsync(new DeleteCollectionRequest(name), DeleteCollectionRequest._ENDPOINT);
  }
}
