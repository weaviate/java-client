package io.weaviate.client6.v1.api.collections.config;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateConfigClient {
  private final RestTransport restTransport;
  private final WeaviateCollectionsClient collectionsClient;

  protected final CollectionDescriptor<?> collection;

  public WeaviateConfigClient(CollectionDescriptor<?> collection, RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.collectionsClient = new WeaviateCollectionsClient(restTransport, grpcTransport);

    this.collection = collection;
  }

  public Optional<CollectionConfig> get() throws IOException {
    return collectionsClient.getConfig(collection.name());
  }

  public void addProperty(Property property) throws IOException {
    this.restTransport.performRequest(new AddPropertyRequest(collection.name(), property),
        AddPropertyRequest._ENDPOINT);
  }

  public void addReference(String propertyName, String... dataTypes) throws IOException {
    this.addProperty(Property.reference(propertyName, dataTypes).toProperty());
  }

  public void update(String collectionName,
      Function<UpdateCollectionRequest.Builder, ObjectBuilder<UpdateCollectionRequest>> fn) throws IOException {
    var thisCollection = get().orElseThrow(); // TODO: use descriptive error
    this.restTransport.performRequest(UpdateCollectionRequest.of(thisCollection, fn),
        UpdateCollectionRequest._ENDPOINT);
  }
}
