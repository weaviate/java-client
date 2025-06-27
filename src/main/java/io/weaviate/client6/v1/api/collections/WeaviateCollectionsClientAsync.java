package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.List;
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

  public CollectionHandleAsync<Map<String, Object>> use(String collectionName) {
    return new CollectionHandleAsync<>(restTransport, grpcTransport,
        CollectionDescriptor.ofMap(collectionName));
  }

  public CompletableFuture<CollectionConfig> create(String name) {
    return create(CollectionConfig.of(name));
  }

  public CompletableFuture<CollectionConfig> create(String name,
      Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) {
    return create(CollectionConfig.of(name, fn));
  }

  public CompletableFuture<CollectionConfig> create(CollectionConfig collection) {
    return this.restTransport.performRequestAsync(new CreateCollectionRequest(collection),
        CreateCollectionRequest._ENDPOINT);
  }

  public CompletableFuture<Optional<CollectionConfig>> getConfig(String name) {
    return this.restTransport.performRequestAsync(new GetConfigRequest(name), GetConfigRequest._ENDPOINT);
  }

  public CompletableFuture<List<CollectionConfig>> list() {
    return this.restTransport.performRequestAsync(new ListCollectionRequest(), ListCollectionRequest._ENDPOINT);
  }

  public CompletableFuture<Void> delete(String name) {
    return this.restTransport.performRequestAsync(new DeleteCollectionRequest(name), DeleteCollectionRequest._ENDPOINT);
  }

  public CompletableFuture<Void> deleteAll() throws IOException {
    return list().thenCompose(collections -> {
      var futures = collections.stream()
          .map(collection -> delete(collection.collectionName()))
          .toArray(CompletableFuture[]::new);
      return CompletableFuture.allOf(futures);
    });
  }

  public CompletableFuture<Boolean> exists(String name) {
    return getConfig(name).thenApply(Optional::isPresent);
  }
}
