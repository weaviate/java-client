package technology.semi.weaviate.client.v1.misc.api;

import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;

public class LiveChecker extends BaseClient<String> implements Client<Boolean> {

  public LiveChecker(Config config) {
    super(config);
  }

  @Override
  public Boolean Do() {
    Response<String> resp = sendGetRequest("/.well-known/live", String.class);
    return resp.getStatusCode() == 200;
  }
}