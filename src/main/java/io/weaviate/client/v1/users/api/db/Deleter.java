package io.weaviate.client.v1.users.api.db;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class Deleter extends BaseClient<Void> implements ClientResult<Boolean> {
  private String userId;

  public Deleter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public Deleter withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    return Result.voidToBoolean(sendDeleteRequest("/users/db/" + userId, null, Void.class));
  }
}
