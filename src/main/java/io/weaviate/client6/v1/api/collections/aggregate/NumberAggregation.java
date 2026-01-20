package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.Set;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public class NumberAggregation
    extends AbstractPropertyAggregation<WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder> {

  public NumberAggregation(String property,
      Set<Metric<WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder>> metrics) {
    super(property, metrics);
  }

  public static NumberAggregation of(String property, Function<Builder, ObjectBuilder<NumberAggregation>> fn) {
    return fn.apply(new Builder(property)).build();
  }

  public NumberAggregation(Builder builder) {
    this(builder.property, builder.metrics);
  }

  public static class Builder extends
      AbstractPropertyAggregation.Builder<WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder, NumberAggregation, Builder> {

    public Builder(String property) {
      super(property);
    }

    public final Builder count() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder::setCount);
    }

    public Builder min() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder::setMinimum);
    }

    public Builder max() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder::setMaximum);
    }

    public Builder mean() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder::setMean);
    }

    public Builder median() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder::setMedian);
    }

    public Builder mode() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder::setMode);
    }

    public Builder sum() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.Builder::setSum);
    }

    @Override
    public final NumberAggregation build() {
      return new NumberAggregation(this);
    }
  }

  public record Values(Long count, Double min, Double max, Double mean, Double median, Double mode, Double sum) {
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Aggregation.Builder req) {
    super.appendTo(req);
    var number = WeaviateProtoAggregate.AggregateRequest.Aggregation.Number.newBuilder();
    appendMetrics(number);
    req.setNumber(number);
  }
}
