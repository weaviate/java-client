package io.weaviate.client6.v1.api.collections.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.ReferenceProperty;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateConfigClientAsync {
  private final RestTransport restTransport;
  private final WeaviateCollectionsClientAsync collectionsClient;

  private final CollectionDescriptor<?> collection;
  private final CollectionHandleDefaults defaults;

  public WeaviateConfigClientAsync(
      CollectionDescriptor<?> collection,
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.restTransport = restTransport;
    this.collectionsClient = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);

    this.collection = collection;
    this.defaults = defaults;
  }

  /** Copy constructor that sets new defaults. */
  public WeaviateConfigClientAsync(WeaviateConfigClientAsync c, CollectionHandleDefaults defaults) {
    this.restTransport = c.restTransport;
    this.collectionsClient = c.collectionsClient;
    this.collection = c.collection;
    this.defaults = defaults;
  }

  public CompletableFuture<Optional<CollectionConfig>> get() throws IOException {
    return collectionsClient.getConfig(collection.collectionName());
  }

  public CompletableFuture<Void> addProperty(Property property) throws IOException {
    return this.restTransport.performRequestAsync(new AddPropertyRequest(collection.collectionName(), property),
        AddPropertyRequest._ENDPOINT);
  }

  public CompletableFuture<Void> addReference(String name, String... dataTypes) throws IOException {
    return this.addProperty(ReferenceProperty.to(name, dataTypes).toProperty());
  }

  public CompletableFuture<Void> update(String collectionName,
      Function<UpdateCollectionRequest.Builder, ObjectBuilder<UpdateCollectionRequest>> fn) throws IOException {
    return get().thenCompose(maybeCollection -> {
      var thisCollection = maybeCollection.orElseThrow();
      return this.restTransport.performRequestAsync(UpdateCollectionRequest.of(thisCollection, fn),
          UpdateCollectionRequest._ENDPOINT);
    });
  }

  public CompletableFuture<List<Shard>> getShards() {
    return this.restTransport.performRequestAsync(null, GetShardsRequest.endpoint(collection, defaults));
  }

  public CompletableFuture<List<Shard>> updateShards(ShardStatus status, String... shards) throws IOException {
    return updateShards(status, Arrays.asList(shards));
  }

  public CompletableFuture<List<Shard>> updateShards(ShardStatus status, List<String> shards) throws IOException {
    var updates = shards.stream().map(
        shard -> this.restTransport.performRequestAsync(
            new UpdateShardStatusRequest(collection.collectionName(), shard, status),
            UpdateShardStatusRequest._ENDPOINT))
        .toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(updates).thenCompose(__ -> getShards());
  }
}
