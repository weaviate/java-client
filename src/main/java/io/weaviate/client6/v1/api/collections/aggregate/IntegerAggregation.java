package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.Set;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public class IntegerAggregation
    extends AbstractPropertyAggregation<WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder> {

  public IntegerAggregation(String property,
      Set<Metric<WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder>> metrics,
      Integer topOccurrencesCutoff) {
    super(property, metrics);
  }

  public static IntegerAggregation of(String property, Function<Builder, ObjectBuilder<IntegerAggregation>> fn) {
    return fn.apply(new Builder(property)).build();
  }

  public IntegerAggregation(Builder builder) {
    this(builder.property, builder.metrics, builder.topOccurrencesCutoff);
  }

  public static class Builder extends
      AbstractPropertyAggregation.Builder<WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder, IntegerAggregation, Builder> {
    private Integer topOccurrencesCutoff;

    public Builder(String property) {
      super(property);
    }

    public final Builder count() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setCount);
    }

    public Builder min() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMinimum);
    }

    public Builder max() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMaximum);
    }

    public Builder mean() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMean);
    }

    public Builder median() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMedian);
    }

    public Builder mode() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMode);
    }

    public Builder sum() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setSum);
    }

    @Override
    public final IntegerAggregation build() {
      return new IntegerAggregation(this);
    }
  }

  public record Values(Long count, Long min, Long max, Double mean, Double median, Long mode, Long sum) {
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Aggregation.Builder req) {
    super.appendTo(req);
    var integer = WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.newBuilder();
    appendMetrics(integer);
    req.setInt(integer);
  }
}
