package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;

public interface WhereOperand {
  void appendTo(WeaviateProtoBase.Filters.Builder where);

  default boolean isEmpty() {
    return false;
  }
}
