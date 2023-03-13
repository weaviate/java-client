package io.weaviate.client.v1.schema.api;

import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

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
