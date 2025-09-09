package io.weaviate.client6.v1.api.collections.query;

import java.util.List;

public record SortBy(List<String> path, boolean ascending) {
  public static SortBy property(String property) {
    return new SortBy(List.of(property), true);
  }

  public static SortBy creationTime() {
    return property("_creationTimeUnix");
  }

  public static SortBy lastUpdateTime() {
    return property("_lastUpdateTimeUnix");
  }

  public SortBy asc() {
    return new SortBy(path, true);
  }

  public SortBy desc() {
    return new SortBy(path, false);
  }
}
