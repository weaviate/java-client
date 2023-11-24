package io.weaviate.client.v1.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.WeaviateClass;

public class ClassUpdater extends BaseClient<WeaviateClass> implements ClientResult<Boolean> {

  private WeaviateClass clazz;

  public ClassUpdater(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ClassUpdater withClass(WeaviateClass clazz) {
    this.clazz = clazz;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    String path = String.format("/schema/%s", UrlEncoder.encodePathParam(clazz.getClassName()));
    Response<WeaviateClass> resp = sendPutRequest(path, clazz, WeaviateClass.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
