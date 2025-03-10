package io.weaviate.client6;

import io.weaviate.client6.v1.collections.Collections;

public class WeaviateClient {
  public final Collections collections;

  // TODO: hide befind an internal HttpClient
  private final Config config;

  public WeaviateClient(Config config) {
    this.config = config;
    this.collections = new Collections(config);
  }
}
