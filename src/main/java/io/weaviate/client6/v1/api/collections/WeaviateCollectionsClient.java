package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateCollectionsClient {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateCollectionsClient(RestTransport restTransport, GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  public CollectionHandle<Map<String, Object>> use(String collectionName) {
    return new CollectionHandle<>(restTransport, grpcTransport, CollectionDescriptor.ofMap(collectionName));
  }

  public CollectionConfig create(String name) throws IOException {
    return create(CollectionConfig.of(name));
  }

  public CollectionConfig create(String name,
      Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) throws IOException {
    return create(CollectionConfig.of(name, fn));
  }

  public CollectionConfig create(CollectionConfig collection) throws IOException {
    return this.restTransport.performRequest(new CreateCollectionRequest(collection),
        CreateCollectionRequest._ENDPOINT);
  }

  public Optional<CollectionConfig> getConfig(String name) throws IOException {
    return this.restTransport.performRequest(new GetConfigRequest(name), GetConfigRequest._ENDPOINT);
  }

  public List<CollectionConfig> list() throws IOException {
    return this.restTransport.performRequest(new ListCollectionRequest(), ListCollectionRequest._ENDPOINT);
  }

  public void delete(String name) throws IOException {
    this.restTransport.performRequest(new DeleteCollectionRequest(name), DeleteCollectionRequest._ENDPOINT);
  }

  public void deleteAll() throws IOException {
    for (var collection : list()) {
      delete(collection.collectionName());
    }
  }

  public boolean exists(String name) throws IOException {
    return getConfig(name).isPresent();
  }
}
