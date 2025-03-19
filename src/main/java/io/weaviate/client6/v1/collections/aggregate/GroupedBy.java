package io.weaviate.client6.v1.collections.aggregate;

public record GroupedBy<T>(String property, T value) {
  public boolean isText() {
    return value instanceof String;
  }
}
