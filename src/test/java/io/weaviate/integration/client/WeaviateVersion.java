package io.weaviate.integration.client;

public class WeaviateVersion {

  // docker image version
  public static final String WEAVIATE_IMAGE = "1.28.6-660c1fa";

  // to be set according to weaviate docker image
  public static final String EXPECTED_WEAVIATE_VERSION = "1.28.6";
  // to be set according to weaviate docker image
  public static final String EXPECTED_WEAVIATE_GIT_HASH = "660c1fa";

  private WeaviateVersion() {
  }
}
