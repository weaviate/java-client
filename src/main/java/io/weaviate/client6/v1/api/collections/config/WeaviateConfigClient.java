package io.weaviate.client6.v1.api.collections.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.ReferenceProperty;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateConfigClient {
  private final RestTransport restTransport;
  private final WeaviateCollectionsClient collectionsClient;

  private final CollectionDescriptor<?> collection;
  private final CollectionHandleDefaults defaults;

  public WeaviateConfigClient(
      CollectionDescriptor<?> collection,
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.restTransport = restTransport;
    this.collectionsClient = new WeaviateCollectionsClient(restTransport, grpcTransport);

    this.collection = collection;
    this.defaults = defaults;
  }

  /** Copy constructor that sets new defaults. */
  public WeaviateConfigClient(WeaviateConfigClient c, CollectionHandleDefaults defaults) {
    this.restTransport = c.restTransport;
    this.collectionsClient = c.collectionsClient;
    this.collection = c.collection;
    this.defaults = defaults;
  }

  public Optional<CollectionConfig> get() throws IOException {
    return collectionsClient.getConfig(collection.collectionName());
  }

  public void addProperty(Property property) throws IOException {
    this.restTransport.performRequest(new AddPropertyRequest(collection.collectionName(), property),
        AddPropertyRequest._ENDPOINT);
  }

  public void addReference(String propertyName, String... dataTypes) throws IOException {
    this.addProperty(ReferenceProperty.to(propertyName, dataTypes).toProperty());
  }

  public void update(Function<UpdateCollectionRequest.Builder, ObjectBuilder<UpdateCollectionRequest>> fn)
      throws IOException {
    var thisCollection = get().orElseThrow(); // TODO: use descriptive error
    this.restTransport.performRequest(UpdateCollectionRequest.of(thisCollection, fn),
        UpdateCollectionRequest._ENDPOINT);
  }

  public List<Shard> getShards() throws IOException {
    return this.restTransport.performRequest(null, GetShardsRequest.endpoint(collection, defaults));
  }

  public List<Shard> updateShards(ShardStatus status, String... shards) throws IOException {
    return updateShards(status, Arrays.asList(shards));
  }

  public List<Shard> updateShards(ShardStatus status, List<String> shards) throws IOException {
    for (var shard : shards) {
      this.restTransport.performRequest(
          new UpdateShardStatusRequest(collection.collectionName(), shard, status),
          UpdateShardStatusRequest._ENDPOINT);
    }
    return getShards();
  }
}
