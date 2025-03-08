package io.weaviate.client6.v1.query;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
public class SearchResult<T> {
  public final List<SearchObject<T>> objects;

  @AllArgsConstructor
  public static class SearchObject<T> {
    public final T properties;
    public final SearchMetadata metadata;

    @AllArgsConstructor
    @ToString
    public static class SearchMetadata {
      String id;
      Float distance;
      Float[] vector;
    }
  }
}
