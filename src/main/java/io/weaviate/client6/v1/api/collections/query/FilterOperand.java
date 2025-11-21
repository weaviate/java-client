package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;

public interface FilterOperand {
  void appendTo(WeaviateProtoBase.Filters.Builder filter);

  default boolean isEmpty() {
    return false;
  }
}
