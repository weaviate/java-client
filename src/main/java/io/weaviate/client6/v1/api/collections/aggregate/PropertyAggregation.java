package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public interface PropertyAggregation {
  void appendTo(WeaviateProtoAggregate.AggregateRequest.Aggregation.Builder req);

  interface Metric<AggregationT> extends Consumer<AggregationT> {
    static <AggregationT> Metric<AggregationT> of(BiConsumer<AggregationT, Boolean> fn) {
      return builder -> fn.accept(builder, true);
    }
  }
}
