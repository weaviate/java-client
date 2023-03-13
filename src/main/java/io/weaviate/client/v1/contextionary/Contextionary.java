package io.weaviate.client.v1.contextionary;

import io.weaviate.client.v1.contextionary.api.ConceptsGetter;
import io.weaviate.client.v1.contextionary.api.ExtensionCreator;
import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;

public class Contextionary {
  private final Config config;
  private final HttpClient httpClient;

  public Contextionary(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public ConceptsGetter conceptsGetter() {
    return new ConceptsGetter(httpClient, config);
  }

  public ExtensionCreator extensionCreator() {
    return new ExtensionCreator(httpClient, config);
  }
}
