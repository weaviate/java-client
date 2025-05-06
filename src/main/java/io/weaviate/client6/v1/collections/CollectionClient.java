package io.weaviate.client6.v1.collections;

import io.weaviate.client6.Config;
import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.collections.aggregate.AggregateClient;
import io.weaviate.client6.v1.collections.data.DataClient;
import io.weaviate.client6.v1.collections.query.QueryClient;

public class CollectionClient<T> {
  public final QueryClient<T> query;
  public final DataClient<T> data;
  public final CollectionConfigClient config;
  public final AggregateClient aggregate;

  public CollectionClient(String collectionName, Config config, GrpcClient grpc, HttpClient http) {
    this.query = new QueryClient<>(collectionName, grpc);
    this.data = new DataClient<>(collectionName, config, http, grpc);
    this.config = new CollectionConfigClient(collectionName, config, http);
    this.aggregate = new AggregateClient(collectionName, grpc);
  }
}
