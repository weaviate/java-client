package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public final class Aggregate {
  /** Prevent public initialization. */
  private Aggregate() {
  }

  public static final PropertyAggregation text(String property,
      Function<TextAggregation.Builder, ObjectBuilder<TextAggregation>> fn) {
    return TextAggregation.of(property, fn);
  }

  public static final PropertyAggregation integer(String property,
      Function<IntegerAggregation.Builder, ObjectBuilder<IntegerAggregation>> fn) {
    return IntegerAggregation.of(property, fn);
  }

  public static final PropertyAggregation bool(String property,
      Function<BooleanAggregation.Builder, ObjectBuilder<BooleanAggregation>> fn) {
    return BooleanAggregation.of(property, fn);
  }

  public static final PropertyAggregation date(String property,
      Function<DateAggregation.Builder, ObjectBuilder<DateAggregation>> fn) {
    return DateAggregation.of(property, fn);
  }

  public static final PropertyAggregation number(String property,
      Function<NumberAggregation.Builder, ObjectBuilder<NumberAggregation>> fn) {
    return NumberAggregation.of(property, fn);
  }
}
