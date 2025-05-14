package io.weaviate.client6.internal.codec.grpc.v1;

import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;

import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.v1.api.collections.query.NearVector;
import io.weaviate.client6.v1.collections.aggregate.AggregateGroupByRequest.GroupBy;
import io.weaviate.client6.v1.collections.aggregate.AggregateRequest;
import io.weaviate.client6.v1.collections.aggregate.IntegerMetric;
import io.weaviate.client6.v1.collections.aggregate.Metric;
import io.weaviate.client6.v1.collections.aggregate.TextMetric;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;

public final class AggregateMarshaler {
  private final WeaviateProtoAggregate.AggregateRequest.Builder req = WeaviateProtoAggregate.AggregateRequest
      .newBuilder();
  private final String collectionName;

  public AggregateMarshaler(String collectionName) {
    this.collectionName = collectionName;
  }

  public WeaviateProtoAggregate.AggregateRequest marshal() {
    return req.build();
  }

  public AggregateMarshaler addAggregation(AggregateRequest aggregate) {
    req.setCollection(collectionName);

    if (aggregate.includeTotalCount()) {
      req.setObjectsCount(true);
    }

    if (aggregate.objectLimit() != null) {
      req.setObjectLimit(aggregate.objectLimit());
    }

    for (Metric metric : aggregate.returnMetrics()) {
      addMetric(metric);
    }

    return this;
  }

  public AggregateMarshaler addGroupBy(GroupBy groupBy) {
    var by = WeaviateProtoAggregate.AggregateRequest.GroupBy.newBuilder();
    by.setCollection(collectionName);
    by.setProperty(groupBy.property());
    req.setGroupBy(by);
    return this;
  }

  public AggregateMarshaler addNearVector(NearVector nv) {
    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();
    nearVector.setVectorBytes(GRPC.toByteString(nv.vector()));

    if (nv.certainty() != null) {
      nearVector.setCertainty(nv.certainty());
    } else if (nv.distance() != null) {
      nearVector.setDistance(nv.distance());
    }

    req.setNearVector(nearVector);

    // Base query options
    if (nv.common().limit() != null) {
      req.setLimit(nv.common().limit());
    }
    return this;
  }

  private void addMetric(Metric metric) {
    var aggregation = WeaviateProtoAggregate.AggregateRequest.Aggregation.newBuilder();
    aggregation.setProperty(metric.property());

    if (metric instanceof TextMetric m) {
      var text = WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.newBuilder();
      m.functions().forEach(f -> set(f, text));
      if (m.atLeast() != null) {
        text.setTopOccurencesLimit(m.atLeast());
      }
      aggregation.setText(text);
    } else if (metric instanceof IntegerMetric m) {
      var integer = WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.newBuilder();
      m.functions().forEach(f -> set(f, integer));
      aggregation.setInt(integer);
    } else {
      assert false : "branch not covered";
    }

    req.addAggregations(aggregation);
  }

  @SuppressWarnings("unchecked")
  static final <B> void set(Enum<?> fn, B builder) {
    if (metrics.containsKey(fn)) {
      ((Toggle<B>) metrics.get(fn)).toggleOn(builder);
    }
  }

  static final ImmutableMap<Enum<?>, Toggle<?>> metrics = new ImmutableMap.Builder<Enum<?>, Toggle<?>>()
      .put(TextMetric._Function.TYPE,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder::setType))
      .put(TextMetric._Function.COUNT,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder::setCount))
      .put(TextMetric._Function.TOP_OCCURRENCES,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder::setTopOccurences))

      .put(IntegerMetric._Function.COUNT,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setCount))
      .put(IntegerMetric._Function.MIN,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMinimum))
      .put(IntegerMetric._Function.MAX,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMaximum))
      .put(IntegerMetric._Function.MEAN,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMean))
      .put(IntegerMetric._Function.MEDIAN,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMedian))
      .put(IntegerMetric._Function.MODE,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setMode))
      .put(IntegerMetric._Function.SUM,
          new Toggle<>(WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer.Builder::setSum))
      .build();

  static class Toggle<B> {
    private final BiConsumer<B, Boolean> setter;

    Toggle(BiConsumer<B, Boolean> setter) {
      this.setter = setter;
    }

    final void toggleOn(B builder) {
      setter.accept(builder, true);
    }
  }

}
