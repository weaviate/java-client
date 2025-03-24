package io.weaviate.client6.internal.codec.grpc.v1;

import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Aggregation;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBaseSearch;
import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.v1.collections.aggregate.AggregateGroupByRequest;
import io.weaviate.client6.v1.collections.aggregate.AggregateGroupByRequest.GroupBy;
import io.weaviate.client6.v1.collections.aggregate.AggregateRequest;
import io.weaviate.client6.v1.collections.aggregate.IntegerMetric;
import io.weaviate.client6.v1.collections.aggregate.Metric;
import io.weaviate.client6.v1.collections.aggregate.TextMetric;
import io.weaviate.client6.v1.query.NearVector;

public final class AggregateMarshaler {
  private final WeaviateProtoAggregate.AggregateRequest.Builder req = WeaviateProtoAggregate.AggregateRequest
      .newBuilder();

  public WeaviateProtoAggregate.AggregateRequest marshal(AggregateGroupByRequest aggregateGroupBy) {
    var aggregate = aggregateGroupBy.aggregate();
    if (aggregateGroupBy.groupBy() != null) {
      addGroupBy(aggregate.collectionName(), aggregateGroupBy.groupBy(), req);
    }
    return marshal(aggregate);
  }

  public WeaviateProtoAggregate.AggregateRequest marshal(NearVector nearVector,
      AggregateGroupByRequest aggregateGroupBy) {
    var aggregate = aggregateGroupBy.aggregate();
    if (aggregateGroupBy.groupBy() != null) {
      addGroupBy(aggregate.collectionName(), aggregateGroupBy.groupBy(), req);
    }
    return marshal(nearVector, aggregate);
  }

  public WeaviateProtoAggregate.AggregateRequest marshal(NearVector nearVector, AggregateRequest aggregate) {
    req.setNearVector(buildNearVector(nearVector));
    if (nearVector.common().limit() != null) {
      req.setLimit(nearVector.common().limit());
    }
    return marshal(aggregate);
  }

  private WeaviateProtoBaseSearch.NearVector buildNearVector(NearVector nv) {
    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();
    nearVector.setVectorBytes(GRPC.toByteString(nv.vector()));

    if (nv.certainty() != null) {
      nearVector.setCertainty(nv.certainty());
    } else if (nv.distance() != null) {
      nearVector.setDistance(nv.distance());
    }
    return nearVector.build();
  }

  public WeaviateProtoAggregate.AggregateRequest marshal(AggregateRequest aggregate) {
    req.setCollection(aggregate.collectionName());

    if (aggregate.includeTotalCount()) {
      req.setObjectsCount(true);
    }

    if (aggregate.objectLimit() != null) {
      req.setObjectLimit(aggregate.objectLimit());
    }

    for (Metric metric : aggregate.returnMetrics()) {
      addMetric(metric);
    }

    return req.build();
  }

  private void addMetric(Metric metric) {
    var aggregation = Aggregation.newBuilder();
    aggregation.setProperty(metric.property());

    if (metric instanceof TextMetric m) {
      var text = Aggregation.Text.newBuilder();
      m.functions().forEach(f -> set(f, text));
      if (m.atLeast() != null) {
        text.setTopOccurencesLimit(m.atLeast());
      }
      aggregation.setText(text);
    } else if (metric instanceof IntegerMetric m) {
      var integer = Aggregation.Integer.newBuilder();
      m.functions().forEach(f -> set(f, integer));
      aggregation.setInt(integer);
    } else {
      assert false : "branch not covered";
    }

    req.addAggregations(aggregation);
  }

  private void addGroupBy(String collectionName, GroupBy groupBy, WeaviateProtoAggregate.AggregateRequest.Builder req) {
    var by = WeaviateProtoAggregate.AggregateRequest.GroupBy.newBuilder();
    by.setCollection(collectionName);
    by.setProperty(groupBy.property());
    req.setGroupBy(by);
  }

  @SuppressWarnings("unchecked")
  static final <B> void set(Enum<?> fn, B builder) {
    if (metrics.containsKey(fn)) {
      ((Toggle<B>) metrics.get(fn)).toggleOn(builder);
    }
  }

  static final ImmutableMap<Enum<?>, Toggle<?>> metrics = new ImmutableMap.Builder<Enum<?>, Toggle<?>>()
      .put(TextMetric._Function.TYPE, new Toggle<>(Aggregation.Text.Builder::setType))
      .put(TextMetric._Function.COUNT, new Toggle<>(Aggregation.Text.Builder::setCount))
      .put(TextMetric._Function.TOP_OCCURRENCES, new Toggle<>(Aggregation.Text.Builder::setTopOccurences))

      .put(IntegerMetric._Function.COUNT, new Toggle<>(Aggregation.Integer.Builder::setCount))
      .put(IntegerMetric._Function.MIN, new Toggle<>(Aggregation.Integer.Builder::setMinimum))
      .put(IntegerMetric._Function.MAX, new Toggle<>(Aggregation.Integer.Builder::setMaximum))
      .put(IntegerMetric._Function.MEAN, new Toggle<>(Aggregation.Integer.Builder::setMean))
      .put(IntegerMetric._Function.MEDIAN, new Toggle<>(Aggregation.Integer.Builder::setMedian))
      .put(IntegerMetric._Function.MODE, new Toggle<>(Aggregation.Integer.Builder::setMode))
      .put(IntegerMetric._Function.SUM, new Toggle<>(Aggregation.Integer.Builder::setSum))
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
