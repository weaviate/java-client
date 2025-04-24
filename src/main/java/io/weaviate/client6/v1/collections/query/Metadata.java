package io.weaviate.client6.v1.collections.query;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;

/**
 * Metadata is the common base for all properties that are requestes as
 * "_additional". It is an inteface all metadata properties MUST implement to be
 * used in {@link CommonQueryOptions}.
 */
public interface Metadata {
  void appendTo(MetadataRequest.Builder metadata);
}
