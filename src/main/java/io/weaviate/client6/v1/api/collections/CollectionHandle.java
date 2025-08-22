package io.weaviate.client6.v1.api.collections;

import java.util.Collection;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClient;
import io.weaviate.client6.v1.api.collections.config.WeaviateConfigClient;
import io.weaviate.client6.v1.api.collections.data.WeaviateDataClient;
import io.weaviate.client6.v1.api.collections.pagination.Paginator;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.api.collections.tenants.WeaviateTenantsClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class CollectionHandle<PropertiesT> {
  public final WeaviateConfigClient config;
  public final WeaviateDataClient<PropertiesT> data;
  public final WeaviateQueryClient<PropertiesT> query;
  public final WeaviateAggregateClient aggregate;
  public final WeaviateTenantsClient tenants;

  private final CollectionHandleDefaults defaults;

  public CollectionHandle(
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    this.config = new WeaviateConfigClient(collection, restTransport, grpcTransport, defaults);
    this.aggregate = new WeaviateAggregateClient(collection, grpcTransport, defaults);
    this.query = new WeaviateQueryClient<>(collection, grpcTransport, defaults);
    this.data = new WeaviateDataClient<>(collection, restTransport, grpcTransport, defaults);
    this.defaults = defaults;

    this.tenants = new WeaviateTenantsClient(collection, restTransport, grpcTransport);
  }

  /** Copy constructor that sets new defaults. */
  private CollectionHandle(CollectionHandle<PropertiesT> c, CollectionHandleDefaults defaults) {
    this.config = new WeaviateConfigClient(c.config, defaults);
    this.aggregate = new WeaviateAggregateClient(c.aggregate, defaults);
    this.query = new WeaviateQueryClient<>(c.query, defaults);
    this.data = new WeaviateDataClient<>(c.data, defaults);
    this.defaults = defaults;

    this.tenants = c.tenants;
  }

  public Paginator<PropertiesT> paginate() {
    return Paginator.of(this.query);
  }

  public Paginator<PropertiesT> paginate(
      Function<Paginator.Builder<PropertiesT>, ObjectBuilder<Paginator<PropertiesT>>> fn) {
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

  public ConsistencyLevel consistencyLevel() {
    return defaults.consistencyLevel();
  }

  public CollectionHandle<PropertiesT> withConsistencyLevel(ConsistencyLevel consistencyLevel) {
    return new CollectionHandle<>(this, CollectionHandleDefaults.of(with -> with.consistencyLevel(consistencyLevel)));
  }

  public String tenant() {
    return defaults.tenant();
  }

  public CollectionHandle<PropertiesT> withTenant(String tenant) {
    return new CollectionHandle<>(this, CollectionHandleDefaults.of(with -> with.tenant(tenant)));
  }

  public CollectionHandle<PropertiesT> withDefaults(
      Function<CollectionHandleDefaults.Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return new CollectionHandle<>(this, CollectionHandleDefaults.of(fn));
  }
}
