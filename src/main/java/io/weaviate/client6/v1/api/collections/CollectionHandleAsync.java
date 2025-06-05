package io.weaviate.client6.v1.api.collections;

import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClientAsync;
import io.weaviate.client6.v1.api.collections.config.WeaviateConfigClientAsync;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClientAsync;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class CollectionHandleAsync<T> {
  public final WeaviateConfigClientAsync config;
  public final WeaviateQueryClientAsync<T> query;
  public final WeaviateAggregateClientAsync aggregate;

  public CollectionHandleAsync(
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionDescriptor<T> collectionDescriptor) {

    this.config = new WeaviateConfigClientAsync(collectionDescriptor, restTransport, grpcTransport);
    this.query = new WeaviateQueryClientAsync<>(collectionDescriptor, grpcTransport);
    this.aggregate = new WeaviateAggregateClientAsync(collectionDescriptor, grpcTransport);
  }
}
