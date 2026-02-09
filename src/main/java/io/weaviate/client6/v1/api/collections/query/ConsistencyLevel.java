package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public enum ConsistencyLevel {
  /**
   * The operation succeeds as soon as one replica acknowledges the request. This
   * is the fastest and most available, but least consistent option.
   */
  ONE(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE, "ONE"),
  /**
   * The operation succeeds when a majority of replicas (calculated as
   * replication_factor/2 + 1) respond. This provides a balance between
   * consistency and availability.
   */
  QUORUM(WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE, "QUORUM"),
  /**
   * The operation succeeds only when all replicas respond. This is the most
   * consistent but least available and slowest option.
   */
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

  public final void appendTo(WeaviateProtoBatch.BatchStreamRequest.Start.Builder req) {
    req.setConsistencyLevel(consistencyLevel);
  }

  @Override
  public String toString() {
    return queryParameter;
  }
}
