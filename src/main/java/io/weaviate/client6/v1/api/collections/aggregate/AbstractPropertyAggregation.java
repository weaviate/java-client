package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public abstract class AbstractPropertyAggregation<AggregationT> implements PropertyAggregation {
  private final String property;
  private final Set<Metric<AggregationT>> metrics;

  public AbstractPropertyAggregation(String property, Set<Metric<AggregationT>> metrics) {
    this.property = property;
    this.metrics = metrics;
  }

  @SuppressWarnings("unchecked")
  public abstract static class Builder<AggregationT, ObjectT extends AbstractPropertyAggregation<AggregationT>, SELF extends Builder<AggregationT, ObjectT, SELF>>
      implements ObjectBuilder<ObjectT> {
    // Required parameters.
    protected final String property;

    protected final Set<Metric<AggregationT>> metrics = new HashSet<>();

    public Builder(String property) {
      this.property = property;
    }

    protected SELF addMetric(BiConsumer<AggregationT, Boolean> fn) {
      metrics.add(Metric.of(fn));
      return (SELF) this;
    }
  }

  protected final void appendMetrics(AggregationT builder) {
    metrics.forEach(metric -> metric.accept(builder));
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Aggregation.Builder req) {
    req.setProperty(property);
  }
}
