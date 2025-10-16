package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;

public interface NearVectorTarget extends Target {
  void appendVectors(WeaviateProtoBaseSearch.NearVector.Builder req);
}
