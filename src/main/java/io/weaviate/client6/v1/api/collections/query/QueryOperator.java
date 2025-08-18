package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

interface QueryOperator {
  /** Append QueryOperator to the request message. */
  void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req);

  /**
   * Append QueryOperator to the request message and apply default parameters.
   * Implementations generally shouldn't override this method.
   */
  default void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req, CollectionHandleDefaults defaults) {
    appendTo(req);
    if (!req.hasConsistencyLevel() && defaults.consistencyLevel() != null) {
      defaults.consistencyLevel().appendTo(req);
    }
  }
}
