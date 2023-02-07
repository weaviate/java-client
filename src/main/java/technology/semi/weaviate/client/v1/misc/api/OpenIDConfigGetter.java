package technology.semi.weaviate.client.v1.misc.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.misc.model.OpenIDConfiguration;

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
