package io.weaviate.integration.client;

public class WeaviateVersion {

  // docker image version
  public static final String WEAVIATE_IMAGE = "1.29.0";

  // to be set according to weaviate docker image
  public static final String EXPECTED_WEAVIATE_VERSION = "1.29.0";
  // to be set according to weaviate docker image
  public static final String EXPECTED_WEAVIATE_GIT_HASH = "35d800d";

  private WeaviateVersion() {
  }
}
