package io.weaviate.client6.v1.api.collections.aggregate;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest.Builder;

// TODO: move Near-, Hybrid, BM25 under client.collection.operators? With them implementing query.SearchOperator and aggregate.ObjectFilter
public interface ObjectFilter {
  void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req);

  static ObjectFilter NONE = new ObjectFilter() {
    @Override
    public void appendTo(Builder req) {
      return;
    }
  };
}
