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
   * Sort by object's UUID. Ascending order by default.
   *
   * <p>
   * Sorting by UUID may be useful if objects are assigned
   * custom UUIDv7 at ingestion, as those are "time-ordered".
   *
   * <p>
   * It may be less useful for the auto-generated UUIDs,
   * which will produce an essentialy random, albeit stable, order.
   *
   * @see #desc() to sort in descending order.
   */
  public static SortBy uuid() {
    return property(FetchObjectById.ID_PROPERTY);
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
