package io.weaviate.client6.v1.api.collections;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record CollectionHandleDefaults(Optional<ConsistencyLevel> consistencyLevel, Optional<String> tenant) {
  /**
   * Set default values for query / aggregation requests.
   *
   * @return CollectionHandleDefaults derived from applying {@code fn} to
   *         {@link Builder}.
   */
  public static CollectionHandleDefaults of(Function<Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return fn.apply(new Builder()).build();
  }

  /**
   * Empty collection defaults.
   *
   * @return A tucked builder that does not leaves all defaults unset.
   */
  public static Function<Builder, ObjectBuilder<CollectionHandleDefaults>> none() {
    return ObjectBuilder.identity();
  }

  public CollectionHandleDefaults {
    requireNonNull(consistencyLevel, "consistencyLevel is null");
  }

  public CollectionHandleDefaults(Builder builder) {
    this(Optional.of(builder.consistencyLevel), Optional.of(builder.tenant));
  }

  public static final class Builder implements ObjectBuilder<CollectionHandleDefaults> {
    private ConsistencyLevel consistencyLevel;
    private String tenant;

    /** Set default consistency level for this collection handle. */
    public Builder consistencyLevel(ConsistencyLevel consistencyLevel) {
      this.consistencyLevel = consistencyLevel;
      return this;
    }

    /** Set default tenant for this collection handle. */
    public Builder tenant(String tenant) {
      this.tenant = tenant;
      return this;
    }

    @Override
    public CollectionHandleDefaults build() {
      return new CollectionHandleDefaults(this);
    }
  }

  /** Serialize default values to a URL query. */
  public Map<String, Object> queryParameters() {
    if (consistencyLevel.isEmpty() && tenant.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, Object> query = new HashMap<String, Object>();
    consistencyLevel.ifPresent(v -> query.put("consistency_level", v));
    tenant.ifPresent(v -> query.put("tenant", v));
    return query;
  }
}
