package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClientAsync;
import io.weaviate.client6.v1.api.collections.config.WeaviateConfigClientAsync;
import io.weaviate.client6.v1.api.collections.data.WeaviateDataClientAsync;
import io.weaviate.client6.v1.api.collections.pagination.AsyncPaginator;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class CollectionHandleAsync<PropertiesT> {
  public final WeaviateConfigClientAsync config;
  public final WeaviateDataClientAsync<PropertiesT> data;
  public final WeaviateQueryClientAsync<PropertiesT> query;
  public final WeaviateAggregateClientAsync aggregate;

  public CollectionHandleAsync(
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionDescriptor<PropertiesT> collectionDescriptor) {

    this.config = new WeaviateConfigClientAsync(collectionDescriptor, restTransport, grpcTransport);
    this.query = new WeaviateQueryClientAsync<>(collectionDescriptor, grpcTransport);
    this.data = new WeaviateDataClientAsync<>(collectionDescriptor, restTransport, this.query);
    this.aggregate = new WeaviateAggregateClientAsync(collectionDescriptor, grpcTransport);
  }

  public AsyncPaginator<PropertiesT> paginate() {
    return AsyncPaginator.of(this.query);
  }

  public AsyncPaginator<PropertiesT> paginate(
      Function<AsyncPaginator.Builder<PropertiesT>, ObjectBuilder<AsyncPaginator<PropertiesT>>> fn) {
    return AsyncPaginator.of(this.query, fn);
  }
}
