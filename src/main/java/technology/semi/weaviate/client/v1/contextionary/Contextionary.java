package technology.semi.weaviate.client.v1.contextionary;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.contextionary.api.ConceptsGetter;
import technology.semi.weaviate.client.v1.contextionary.api.ExtensionCreator;

public class Contextionary {
  private Config config;

  public Contextionary(Config config) {
    this.config = config;
  }

  public ConceptsGetter conceptsGetter() {
    return new ConceptsGetter(config);
  }

  public ExtensionCreator extensionCreator() {
    return new ExtensionCreator(config);
  }
}
