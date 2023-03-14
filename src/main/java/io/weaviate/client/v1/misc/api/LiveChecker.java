package io.weaviate.client.v1.misc.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.DbVersionProvider;

public class LiveChecker extends BaseClient<String> implements ClientResult<Boolean> {

  private final DbVersionProvider dbVersionProvider;

  public LiveChecker(HttpClient httpClient, Config config, DbVersionProvider dbVersionProvider) {
    super(httpClient, config);
    this.dbVersionProvider = dbVersionProvider;
  }

  @Override
  public Result<Boolean> run() {
    Response<String> resp = sendGetRequest("/.well-known/live", String.class);
    if (resp.getStatusCode() == 200) {
      dbVersionProvider.refresh();
    }
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
