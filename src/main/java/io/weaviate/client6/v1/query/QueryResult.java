package io.weaviate.client6.v1.query;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
public class QueryResult<T> {
  public final List<SearchObject<T>> objects;

  @AllArgsConstructor
  public static class SearchObject<T> {
    public final T properties;
    public final QueryMetadata metadata;

    @AllArgsConstructor
    @ToString
    public static class QueryMetadata {
      String id;
      Float distance;
      // TODO: use Vectors (to handle both Float[] and Float[][])
      Float[] vector;
    }
  }
}
