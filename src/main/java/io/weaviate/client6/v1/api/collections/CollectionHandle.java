package io.weaviate.client6.v1.api.collections;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.weaviate.client6.v1.api.collections.aggregate.WeaviateAggregateClient;
import io.weaviate.client6.v1.api.collections.config.WeaviateConfigClient;
import io.weaviate.client6.v1.api.collections.data.WeaviateDataClient;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
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
    this.query = new WeaviateQueryClient<>(collectionDescriptor, grpcTransport);
    this.data = new WeaviateDataClient<>(collectionDescriptor, restTransport, this.query);
    this.aggregate = new WeaviateAggregateClient(collectionDescriptor, grpcTransport);
  }

  public Stream<WeaviateObject<T, Object, QueryMetadata>> stream() {
    return StreamSupport.stream(spliterator(2), false);
  }

  public Iterable<WeaviateObject<T, Object, QueryMetadata>> list() {
    return () -> Spliterators.iterator(spliterator(2));
  }

  private Spliterator<WeaviateObject<T, Object, QueryMetadata>> spliterator(int batchSize) {
    return new CursorSpliterator<>(batchSize,
        (after, limit) -> this.query.fetchObjects(
            query -> query.after(after).limit(limit)).objects());
  }
}
