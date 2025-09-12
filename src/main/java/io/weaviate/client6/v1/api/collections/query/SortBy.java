package io.weaviate.client6.v1.api.collections.query;

import java.util.List;

public record SortBy(List<String> path, boolean ascending) {
  /**
   * Sort by object property. Ascending order by default.
   *
   * @see #desc() to sort in descending order.
   */
  public static SortBy property(String property) {
    return new SortBy(List.of(property), true);
  }

  /**
   * Sort by object creation time. Ascending order by default.
   *
   * @see #desc() to sort in descending order.
   */
  public static SortBy creationTime() {
    return property("_creationTimeUnix");
  }

  /**
   * Sort by object last update time. Ascending order by default.
   *
   * @see #desc() to sort in descending order.
   */
  public static SortBy lastUpdateTime() {
    return property("_lastUpdateTimeUnix");
  }

  /**
   * Sort in ascending order.
   *
   * <p>
   * Example:
   *
   * <pre>{@code
   * SortBy.property("name").asc();
   * }</pre>
   */
  public SortBy asc() {
    return new SortBy(path, true);
  }

  /**
   * Sort in descending order.
   *
   * <p>
   * Example:
   *
   * <pre>{@code
   * SortBy.property("name").desc();
   * }</pre>
   */
  public SortBy desc() {
    return new SortBy(path, false);
  }
}
