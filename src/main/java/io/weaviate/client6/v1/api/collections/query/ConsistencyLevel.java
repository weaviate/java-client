package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public enum ConsistencyLevel {
  ONE(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE, "ONE"),
  QUORUM(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE, "QUORUM"),
  ALL(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE, "ALL");

  private final WeaviateProtoBase.ConsistencyLevel consistencyLevel;
  private final String queryParameter;

  ConsistencyLevel(WeaviateProtoBase.ConsistencyLevel consistencyLevel, String queryParameter) {
    this.consistencyLevel = consistencyLevel;
    this.queryParameter = queryParameter;
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

  @Override
  public String toString() {
    return queryParameter;
  }
}
