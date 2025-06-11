package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

/**
 * Metadata is the common base for all properties that are requestes as
 * "_additional". It is an inteface all metadata properties MUST implement to be
 * used in {@link BaseQueryOptions}.
 */
public interface Metadata {
  void appendTo(WeaviateProtoSearchGet.MetadataRequest.Builder metadata);

  public static final Metadata ID = MetadataField.ID;
  public static final Metadata VECTOR = MetadataField.VECTOR;
  public static final Metadata DISTANCE = MetadataField.DISTANCE;
  public static final Metadata CERTAINTY = MetadataField.CERTAINTY;
}
