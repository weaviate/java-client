package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public enum ConsistencyLevel {
  ONE(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE),
  QUORUM(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE),
  ALL(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE);

  private final WeaviateProtoBase.ConsistencyLevel consistencyLevel;

  ConsistencyLevel(WeaviateProtoBase.ConsistencyLevel consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
  }

  public final void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    req.setConsistencyLevel(consistencyLevel);
  }

  public final void appendTo(WeaviateProtoBatchDelete.BatchDeleteRequest.Builder req) {
    req.setConsistencyLevel(consistencyLevel);
  }

  public final void appendTo(WeaviateProtoBatch.BatchObjectsRequest.Builder req) {
    req.setConsistencyLevel(consistencyLevel);
  }
}
