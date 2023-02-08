package technology.semi.weaviate.client.v1.misc.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.util.DbVersionProvider;

public class ReadyChecker extends BaseClient<String> implements ClientResult<Boolean> {

  private final DbVersionProvider dbVersionProvider;

  public ReadyChecker(HttpClient httpClient, Config config, DbVersionProvider dbVersionProvider) {
    super(httpClient, config);
    this.dbVersionProvider = dbVersionProvider;
  }

  @Override
  public Result<Boolean> run() {
    Response<String> resp = sendGetRequest("/.well-known/ready", String.class);
    if (resp.getStatusCode() == 200) {
      dbVersionProvider.refresh();
    }
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
