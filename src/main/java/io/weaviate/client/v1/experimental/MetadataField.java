package io.weaviate.client.v1.experimental;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;

/**
 * MetadataField are collection properties that can be requested for any object.
 */
public enum MetadataField implements Metadata {
  ID("id"),
  VECTOR("vector"),
  DISTANCE("distance");

  private final String name;

  private MetadataField(String name) {
    this.name = name;
  }

  // FIXME: ideally, we don't want to surface this method in the public API
  public void append(MetadataRequest.Builder metadata) {
    switch (this.name) {
      case "id":
        metadata.setUuid(true);
        break;
      case "vector":
        metadata.setVector(true);
        break;
      case "distance":
        metadata.setDistance(true);
        break;
    }
  }
}
