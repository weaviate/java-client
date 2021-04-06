package technology.semi.weaviate.client.v1.misc.api;

import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;

public class ReadyChecker extends BaseClient<String> implements Client<Boolean> {

  public ReadyChecker(Config config) {
    super(config);
  }

  @Override
  public Boolean run() {
    Response<String> resp = sendGetRequest("/.well-known/ready", String.class);
    return resp.getStatusCode() == 200;
  }
}
