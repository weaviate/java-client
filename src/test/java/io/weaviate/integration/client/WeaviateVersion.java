package io.weaviate.integration.client;

public class WeaviateVersion {

  // docker image version
  public static final String WEAVIATE_IMAGE = "stable-v1.27-b228431";

  // to be set according to weaviate docker image
  public static final String EXPECTED_WEAVIATE_VERSION = "1.27.0";
  // to be set according to weaviate docker image
  public static final String EXPECTED_WEAVIATE_GIT_HASH = "b228431";

  private WeaviateVersion() {}
}
