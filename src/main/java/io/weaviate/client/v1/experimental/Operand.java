package io.weaviate.client.v1.experimental;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.Filters;

public interface Operand {
  void append(Filters.Builder where);
}
