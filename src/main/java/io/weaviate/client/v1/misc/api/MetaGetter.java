package io.weaviate.client.v1.misc.api;

import io.weaviate.client.v1.misc.model.Meta;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class MetaGetter extends BaseClient<Meta> implements ClientResult<Meta> {

  public MetaGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<Meta> run() {
    Response<Meta> resp = sendGetRequest("/meta", Meta.class);
    return new Result<>(resp);
  }
}
