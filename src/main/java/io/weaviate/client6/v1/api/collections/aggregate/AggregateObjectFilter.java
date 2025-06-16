package io.weaviate.client6.v1.api.collections.aggregate;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest.Builder;

// TODO: move Near-, Hybrid, BM25 under client.collection.operators? With them implementing query.SearchOperator and aggregate.ObjectFilter
public interface AggregateObjectFilter {
  void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req);

  static AggregateObjectFilter NONE = new AggregateObjectFilter() {
    @Override
    public void appendTo(Builder req) {
    }
  };
}
