package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record CollectionHandleDefaults(ConsistencyLevel consistencyLevel) {
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
   * @return An tucked builder that does not leaves all defaults unset.
   */
  public static Function<Builder, ObjectBuilder<CollectionHandleDefaults>> none() {
    return ObjectBuilder.identity();
  }

  public CollectionHandleDefaults(Builder builder) {
    this(builder.consistencyLevel);
  }

  public static final class Builder implements ObjectBuilder<CollectionHandleDefaults> {
    private ConsistencyLevel consistencyLevel;

    public Builder consistencyLevel(ConsistencyLevel consistencyLevel) {
      this.consistencyLevel = consistencyLevel;
      return this;
    }

    @Override
    public CollectionHandleDefaults build() {
      return new CollectionHandleDefaults(this);
    }
  }
}
