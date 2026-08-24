package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public interface QueryOperator {
  default BaseQueryOptions common() {
    return null;
  }

  /**
   * Reranking requested for this query, if any.
   *
   * <p>
   * Reranking is a common query option, so operators that carry
   * {@link BaseQueryOptions} read it from there.
   */
  default Rerank rerank() {
    return common() != null ? common().rerank() : null;
  }

  /** Append QueryOperator to the request message. */
  void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req);
}
