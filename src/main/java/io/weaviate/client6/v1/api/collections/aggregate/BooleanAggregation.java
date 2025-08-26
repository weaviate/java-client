package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.Set;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public class BooleanAggregation
    extends AbstractPropertyAggregation<WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder> {

  public BooleanAggregation(String property,
      Set<Metric<WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder>> metrics) {
    super(property, metrics);
  }

  public static BooleanAggregation of(String property, Function<Builder, ObjectBuilder<BooleanAggregation>> fn) {
    return fn.apply(new Builder(property)).build();
  }

  public BooleanAggregation(Builder builder) {
    this(builder.property, builder.metrics);
  }

  public static class Builder extends
      AbstractPropertyAggregation.Builder<WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder, BooleanAggregation, Builder> {

    public Builder(String property) {
      super(property);
    }

    public final Builder count() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder::setCount);
    }

    public final Builder percentageFalse() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder::setPercentageFalse);
    }

    public final Builder percentageTrue() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder::setPercentageTrue);
    }

    public final Builder totalFalse() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder::setTotalFalse);
    }

    public final Builder totalTrue() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.Builder::setTotalTrue);
    }

    @Override
    public final BooleanAggregation build() {
      return new BooleanAggregation(this);
    }
  }

  public record Values(Long count, Float percentageFalse, Float percentageTrue, Long totalFalse, Long totalTrue) {
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Aggregation.Builder req) {
    super.appendTo(req);
    var bool = WeaviateProtoAggregate.AggregateRequest.Aggregation.Boolean.newBuilder();
    appendMetrics(bool);
    req.setBoolean(bool);
  }
}
