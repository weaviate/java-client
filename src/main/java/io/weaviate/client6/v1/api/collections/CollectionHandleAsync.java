package io.weaviate.client6.v1.api.collections;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateResponse;
import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClientAsync;
import io.weaviate.client6.v1.api.collections.config.WeaviateConfigClientAsync;
import io.weaviate.client6.v1.api.collections.data.WeaviateDataClientAsync;
import io.weaviate.client6.v1.api.collections.pagination.AsyncPaginator;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
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

  private final CollectionHandleDefaults defaults;

  public CollectionHandleAsync(
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionDescriptor<PropertiesT> collectionDescriptor,
      CollectionHandleDefaults defaults) {

    this.config = new WeaviateConfigClientAsync(collectionDescriptor, restTransport, grpcTransport);
    this.aggregate = new WeaviateAggregateClientAsync(collectionDescriptor, grpcTransport, defaults);
    this.query = new WeaviateQueryClientAsync<>(collectionDescriptor, grpcTransport, defaults);
    this.data = new WeaviateDataClientAsync<>(collectionDescriptor, restTransport, grpcTransport, defaults);

    this.defaults = defaults;
  }

  /** Copy constructor that sets new defaults. */
  private CollectionHandleAsync(CollectionHandleAsync<PropertiesT> c, CollectionHandleDefaults defaults) {
    this.config = c.config;
    this.aggregate = c.aggregate;
    this.query = new WeaviateQueryClientAsync<>(c.query, defaults);
    this.data = new WeaviateDataClientAsync<>(c.data, defaults);

    this.defaults = defaults;
  }

  public AsyncPaginator<PropertiesT> paginate() {
    return AsyncPaginator.of(this.query);
  }

  public AsyncPaginator<PropertiesT> paginate(
      Function<AsyncPaginator.Builder<PropertiesT>, ObjectBuilder<AsyncPaginator<PropertiesT>>> fn) {
    return AsyncPaginator.of(this.query, fn);
  }

  /**
   * Get the object count in this collection.
   *
   * <p>
   * While made to resemeble {@link Collection#size}, counting Weaviate collection
   * objects involves making a network call; still, this operation is
   * non-blocking, as resolving the underlying {@code CompletableFuture} is
   * deferred to the caller.
   *
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
  public CompletableFuture<Long> size() {
    return this.aggregate.overAll(all -> all.includeTotalCount(true))
        .thenApply(AggregateResponse::totalCount);
  }

  public ConsistencyLevel consistencyLevel() {
    return defaults.consistencyLevel();
  }

  public CollectionHandleAsync<PropertiesT> withConsistencyLevel(ConsistencyLevel consistencyLevel) {
    return new CollectionHandleAsync<>(this, CollectionHandleDefaults.of(
        def -> def.consistencyLevel(consistencyLevel)));
  }

  public String tenant() {
    return defaults.tenant();
  }

  public CollectionHandleAsync<PropertiesT> withTenant(String tenant) {
    return new CollectionHandleAsync<>(this, CollectionHandleDefaults.of(with -> with.tenant(tenant)));
  }

  public CollectionHandleAsync<PropertiesT> withDefaults(
      Function<CollectionHandleDefaults.Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return new CollectionHandleAsync<>(this, CollectionHandleDefaults.of(fn));
  }
}
