package io.weaviate.client6.v1.api.collections.config;

import java.io.IOException;
import java.util.Optional;

import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.WeaviateCollection;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateConfigClient {
  private final RestTransport transport;
  private final WeaviateCollectionsClient collectionsClient;

  protected final CollectionDescriptor<?> collection;

  public WeaviateConfigClient(CollectionDescriptor<?> collection, RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.transport = restTransport;
    this.collectionsClient = new WeaviateCollectionsClient(restTransport, grpcTransport);

    this.collection = collection;
  }

  public Optional<WeaviateCollection> get() throws IOException {
    return collectionsClient.getConfig(collection.name());
  }

  public void addProperty(Property property) throws IOException {
    this.transport.performRequest(new AddPropertyRequest(collection.name(), property), AddPropertyRequest._ENDPOINT);
  }

  public void addReference(String name, String... dataTypes) throws IOException {
    this.addProperty(Property.reference(name, dataTypes).toProperty());
  }
}
