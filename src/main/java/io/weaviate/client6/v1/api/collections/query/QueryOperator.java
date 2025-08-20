package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

interface QueryOperator {
  /** Append QueryOperator to the request message. */
  void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req);
}
