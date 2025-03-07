package io.weaviate.client.v1.experimental;

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
      public final String id;
      public final Float distance;
      public final Float[] vector;
    }
  }
}
