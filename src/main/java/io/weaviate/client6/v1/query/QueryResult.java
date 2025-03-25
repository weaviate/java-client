package io.weaviate.client6.v1.query;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QueryResult<T> {
  public final List<SearchObject<T>> objects;

  @AllArgsConstructor
  public static class SearchObject<T> {
    public final T properties;
    public final QueryMetadata metadata;

    public record QueryMetadata(String id, Float distance, Float[] vector) {
      // TODO: use Vectors (to handle both Float[] and Float[][])
    }
  }
}
