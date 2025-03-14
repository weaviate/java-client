package io.weaviate.client6.v1.query;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;

/**
 * MetadataField are collection properties that can be requested for any object.
 */
public enum MetadataField implements Metadata {
  ID,
  VECTOR,
  DISTANCE;

  // FIXME: ideally, we don't want to surface this method in the public API
  // But we might have to, if we want to implement that QueryAppender interface.
  public void appendTo(MetadataRequest.Builder metadata) {
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
