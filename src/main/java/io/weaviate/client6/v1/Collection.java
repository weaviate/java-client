package io.weaviate.client6.v1;

import io.weaviate.client6.Config;
import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.collections.WeaviateCollectionConfig;
import io.weaviate.client6.v1.collections.aggregate.WeaviateAggregate;
import io.weaviate.client6.v1.data.Data;
import io.weaviate.client6.v1.query.Query;

public class Collection<T> {
  public final Query<T> query;
  public final Data<T> data;
  public final WeaviateCollectionConfig config;
  public final WeaviateAggregate aggregate;

  public Collection(String collectionName, Config config, GrpcClient grpc, HttpClient http) {
    this.query = new Query<>(collectionName, grpc);
    this.data = new Data<>(collectionName, config, http);
    this.config = new WeaviateCollectionConfig(collectionName, config, http);
    this.aggregate = new WeaviateAggregate(collectionName, grpc);
  }
}
