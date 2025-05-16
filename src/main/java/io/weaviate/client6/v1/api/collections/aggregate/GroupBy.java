package io.weaviate.client6.v1.api.collections.aggregate;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public record GroupBy(String property) {
  public static final GroupBy of(String property) {
    return new GroupBy(property);
  }

  void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req, String collection) {
    req.setGroupBy(WeaviateProtoAggregate.AggregateRequest.GroupBy.newBuilder()
        .setCollection(collection)
        .setProperty(property));
  }
}
