package technology.semi.weaviate.client.v1.contextionary;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.contextionary.api.ConceptsGetter;
import technology.semi.weaviate.client.v1.contextionary.api.ExtensionCreator;

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
