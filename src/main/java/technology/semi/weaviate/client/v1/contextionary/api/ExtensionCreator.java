package technology.semi.weaviate.client.v1.contextionary.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.contextionary.model.C11yExtension;

public class ExtensionCreator extends BaseClient<Object> implements Client<Boolean> {

  private String concept;
  private String definition;
  private Float weight;

  public ExtensionCreator(Config config) {
    super(config);
    this.weight = 1.0f;
  }

  public ExtensionCreator withConcept(String concept) {
    this.concept = concept;
    return this;
  }

  public ExtensionCreator withDefinition(String definition) {
    this.definition = definition;
    return this;
  }

  public ExtensionCreator withWeight(Float weight) {
    this.weight = weight;
    return this;
  }

  @Override
  public Boolean run() {
    if (weight > 1 || weight < 0) {
      return false;
    }
    C11yExtension extension = C11yExtension.builder()
            .concept(concept)
            .definition(definition)
            .weight(weight)
            .build();
    Response<Object> resp = sendPostRequest("/modules/text2vec-contextionary/extensions", extension, Object.class);
    if (resp.getStatusCode() == 200) {
      return true;
    }
    return false;
  }
}
