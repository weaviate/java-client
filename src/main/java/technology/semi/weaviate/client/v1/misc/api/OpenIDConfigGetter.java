package technology.semi.weaviate.client.v1.misc.api;

import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.misc.model.OpenIDConfiguration;

public class OpenIDConfigGetter extends BaseClient<OpenIDConfiguration> implements Client<OpenIDConfiguration> {

  public OpenIDConfigGetter(Config config) {
    super(config);
  }

  @Override
  public OpenIDConfiguration run() {
    Response<OpenIDConfiguration> resp = sendGetRequest("/.well-known/openid-configuration", OpenIDConfiguration.class);
    return resp.getBody();
  }
}
