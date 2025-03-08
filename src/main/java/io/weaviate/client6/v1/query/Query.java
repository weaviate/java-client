package io.weaviate.client6.v1.query;

import java.util.function.Consumer;

public class Query<T> {

  public SearchResult<T> nearVector(Float[] vector, Consumer<NearVector.Options> options) {
    var query = new NearVector(vector, options);
    return null;
  }
}
