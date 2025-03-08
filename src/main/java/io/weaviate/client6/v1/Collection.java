package io.weaviate.client6.v1;

import io.weaviate.client6.Config;
import io.weaviate.client6.v1.data.Data;
import io.weaviate.client6.v1.query.Query;

public class Collection<T> {
  public final Query<T> query;
  public final Data<T> data;

  public Collection(Config config, String collectionName) {
    this.query = new Query<>();
    this.data = new Data<>(collectionName, config);
  }
}
