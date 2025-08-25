package io.weaviate.client6.v1.api.collections.aggregate;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public class DateAggregation
    extends AbstractPropertyAggregation<WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder> {

  public DateAggregation(String property,
      Set<Metric<WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder>> metrics) {
    super(property, metrics);
  }

  public static DateAggregation of(String property, Function<Builder, ObjectBuilder<DateAggregation>> fn) {
    return fn.apply(new Builder(property)).build();
  }

  public DateAggregation(Builder builder) {
    this(builder.property, builder.metrics);
  }

  public static class Builder extends
      AbstractPropertyAggregation.Builder<WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder, DateAggregation, Builder> {

    public Builder(String property) {
      super(property);
    }

    public final Builder count() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder::setCount);
    }

    public Builder min() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder::setMinimum);
    }

    public Builder max() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder::setMaximum);
    }

    public Builder median() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder::setMedian);
    }

    public Builder mode() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.Builder::setMode);
    }

    @Override
    public final DateAggregation build() {
      return new DateAggregation(this);
    }
  }

  public record Values(Long count, OffsetDateTime min, OffsetDateTime max, OffsetDateTime median, OffsetDateTime mode) {
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Aggregation.Builder req) {
    super.appendTo(req);
    var date = WeaviateProtoAggregate.AggregateRequest.Aggregation.Date.newBuilder();
    appendMetrics(date);
    req.setDate(date);
  }
}
