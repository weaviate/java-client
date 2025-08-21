package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record CollectionHandleDefaults(ConsistencyLevel consistencyLevel, String tenant) {
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

  public CollectionHandleDefaults(Builder builder) {
    this(builder.consistencyLevel, builder.tenant);
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

  public Map<String, Object> queryParameters() {
    if (consistencyLevel == null && tenant == null) {
      return Collections.emptyMap();
    }
    var query = new HashMap<String, Object>();
    if (consistencyLevel != null) {
      query.put("consistency_level", consistencyLevel);
    }
    if (tenant != null) {
      query.put("tenant", tenant);
    }
    return query;
  }
}
