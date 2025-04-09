package io.weaviate.client.v1.users.api.db;

import java.util.Arrays;
import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.model.UserDb;

public class AllGetter extends BaseClient<UserDb[]> implements ClientResult<List<UserDb>> {

  public AllGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<List<UserDb>> run() {
    Response<UserDb[]> resp = sendGetRequest("/users/db", UserDb[].class);
    return new Result<>(resp.getStatusCode(), Arrays.asList(resp.getBody()), resp.getErrors());
  }
}
