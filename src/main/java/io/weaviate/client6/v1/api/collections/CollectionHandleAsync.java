package io.weaviate.client6.v1.api.collections;

import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClientAsync;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClientAsync;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class CollectionHandleAsync<T> {
  public final WeaviateQueryClientAsync<T> query;
  public final WeaviateAggregateClientAsync aggregate;

  public CollectionHandleAsync(
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionDescriptor<T> collectionDescriptor) {

    this.query = new WeaviateQueryClientAsync<>(collectionDescriptor, grpcTransport);
    this.aggregate = new WeaviateAggregateClientAsync(collectionDescriptor, grpcTransport);
  }
}
