package io.weaviate.client.v1.experimental;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SearchResult<T> {
  public final List<SearchObject<T>> objects;

  @AllArgsConstructor
  public static class SearchObject<T> {
    public final T properties;
    public final SearchMetadata metadata;

    @AllArgsConstructor
    public static class SearchMetadata {
      String id;
      Float distance;
      Float[] vector;
    }
  }
}
