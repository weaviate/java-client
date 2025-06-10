package io.weaviate.client6.v1.api.collections;

import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClient;
import io.weaviate.client6.v1.api.collections.config.WeaviateConfigClient;
import io.weaviate.client6.v1.api.collections.data.WeaviateDataClient;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class CollectionHandle<T> {
  public final WeaviateConfigClient config;
  public final WeaviateDataClient<T> data;
  public final WeaviateQueryClient<T> query;
  public final WeaviateAggregateClient aggregate;

  public CollectionHandle(
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionDescriptor<T> collectionDescriptor) {

    this.config = new WeaviateConfigClient(collectionDescriptor, restTransport, grpcTransport);
    this.data = new WeaviateDataClient<>(collectionDescriptor, restTransport);
    this.query = new WeaviateQueryClient<>(collectionDescriptor, grpcTransport);
    this.aggregate = new WeaviateAggregateClient(collectionDescriptor, grpcTransport);
  }
}
