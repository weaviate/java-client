package io.weaviate.client.v1.contextionary.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.contextionary.model.C11yWordsResponse;

public class ConceptsGetter extends BaseClient<C11yWordsResponse> implements ClientResult<C11yWordsResponse> {

  private String concept;

  public ConceptsGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ConceptsGetter withConcept(String concept) {
    this.concept = concept;
    return this;
  }

  @Override
  public Result<C11yWordsResponse> run() {
    String path = String.format("/modules/text2vec-contextionary/concepts/%s", concept);
    Response<C11yWordsResponse> resp = sendGetRequest(path, C11yWordsResponse.class);
    return new Result<>(resp);
  }
}
