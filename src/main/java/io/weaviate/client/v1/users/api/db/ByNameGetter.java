package io.weaviate.client.v1.users.api.db;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.model.UserDb;

public class ByNameGetter extends BaseClient<UserDb> implements ClientResult<UserDb> {
  private String userId;

  public ByNameGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ByNameGetter withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public Result<UserDb> run() {
    return new Result<>(sendGetRequest("/users/db/" + userId, UserDb.class));
  }
}
