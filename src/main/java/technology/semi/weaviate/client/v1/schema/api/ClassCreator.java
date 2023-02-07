package technology.semi.weaviate.client.v1.schema.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.schema.model.WeaviateClass;

public class ClassCreator extends BaseClient<WeaviateClass> implements ClientResult<Boolean> {

  private WeaviateClass clazz;

  public ClassCreator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ClassCreator withClass(WeaviateClass clazz) {
    this.clazz = clazz;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    Response<WeaviateClass> resp = sendPostRequest("/schema", clazz, WeaviateClass.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
