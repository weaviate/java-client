package io.weaviate.client.v1.misc.api;

import io.weaviate.client.v1.misc.model.OpenIDConfiguration;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class OpenIDConfigGetter extends BaseClient<OpenIDConfiguration> implements ClientResult<OpenIDConfiguration> {

  public OpenIDConfigGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<OpenIDConfiguration> run() {
    Response<OpenIDConfiguration> resp = sendGetRequest("/.well-known/openid-configuration", OpenIDConfiguration.class);
    return new Result<>(resp);
  }
}
