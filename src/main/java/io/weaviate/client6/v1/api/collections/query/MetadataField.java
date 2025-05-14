package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

/**
 * MetadataField are collection properties that can be requested for any object.
 */
public enum MetadataField implements Metadata {
  ID,
  VECTOR,
  DISTANCE;

  public void appendTo(WeaviateProtoSearchGet.MetadataRequest.Builder metadata) {
    switch (this) {
      case ID:
        metadata.setUuid(true);
        break;
      case VECTOR:
        metadata.setVector(true);
        break;
      case DISTANCE:
        metadata.setDistance(true);
        break;
    }
  }
}
