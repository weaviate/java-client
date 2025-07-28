package io.weaviate.client6.v1.api.collections;

import java.util.Collection;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClient;
import io.weaviate.client6.v1.api.collections.config.WeaviateConfigClient;
import io.weaviate.client6.v1.api.collections.data.WeaviateDataClient;
import io.weaviate.client6.v1.api.collections.pagination.Paginator;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;
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
    this.data = new WeaviateDataClient<>(collectionDescriptor, restTransport, grpcTransport);
    this.query = new WeaviateQueryClient<>(collectionDescriptor, grpcTransport);
    this.aggregate = new WeaviateAggregateClient(collectionDescriptor, grpcTransport);
  }

  public Paginator<T> paginate() {
    return Paginator.of(this.query);
  }

  public Paginator<T> paginate(Function<Paginator.Builder<T>, ObjectBuilder<Paginator<T>>> fn) {
    return Paginator.of(this.query, fn);
  }

  /**
   * Get the object count in this collection.
   *
   * <p>
   * While made to resemeble {@link Collection#size}, counting Weaviate collection
   * objects involves making a network call, making this a blocking operation.
   * This method also does not define behaviour for cases where the size of the
   * collection exceeds {@link Long#MAX_VALUE} as this is unlikely to happen.
   *
   * <p>
   * This is a shortcut for:
   *
   * <pre>{@code
   * handle.aggregate.overAll(all -> all.includeTotalCount(true)).totalCount()
   * }</pre>
   */
  public long size() {
    return this.aggregate.overAll(all -> all.includeTotalCount(true)).totalCount();
  }
}
