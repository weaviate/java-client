package io.weaviate.client.v1.experimental;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;

/**
 * Metadata is the common base for all properties that are requestes as
 * "_additional". It is an inteface all metadata properties MUST implement to be
 * used in {@link SearchOptions}.
 */
public interface Metadata {
  void append(MetadataRequest.Builder metadata);
}
