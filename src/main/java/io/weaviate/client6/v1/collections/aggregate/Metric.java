package io.weaviate.client6.v1.collections.aggregate;

import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Aggregation;

public abstract class Metric<M extends Metric<M>> {
  protected final String property;

  abstract void appendTo(Aggregation.Builder aggregation);

  public Metric(String property) {
    this.property = property;
  }

  public static TextMetric text(String property) {
    return new TextMetric(property, _options -> {
    });
  }

  public static TextMetric text(String property, Consumer<TextMetric.Builder> options) {
    return new TextMetric(property, options);
  }

  public static IntegerMetric integer(String property) {
    return new IntegerMetric(property, _options -> {
    });
  }

  public static IntegerMetric integer(String property, Consumer<IntegerMetric.Builder> options) {
    return new IntegerMetric(property, options);
  }

  public interface Values {
    Long count();
  }
}
