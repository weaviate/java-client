package technology.semi.weaviate.client.v1.contextionary.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.contextionary.model.C11yWordsResponse;

public class ConceptsGetter extends BaseClient<C11yWordsResponse> implements Client<C11yWordsResponse> {

  private String concept;

  public ConceptsGetter(Config config) {
    super(config);
  }

  public ConceptsGetter withConcept(String concept) {
    this.concept = concept;
    return this;
  }

  @Override
  public C11yWordsResponse run() {
    String path = String.format("/modules/text2vec-contextionary/concepts/%s", concept);
    Response<C11yWordsResponse> resp = sendGetRequest(path, C11yWordsResponse.class);
    if (resp.getStatusCode() == 200) {
      return resp.getBody();
    }
    return null;
  }
}
