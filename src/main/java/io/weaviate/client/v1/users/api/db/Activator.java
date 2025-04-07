package io.weaviate.client.v1.users.api.db;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class Activator extends BaseClient<Void> implements ClientResult<Boolean> {
  private String userId;

  public Activator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public Activator withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    return Result.voidToBoolean(sendPostRequest("/users/db/" + userId + "/activate", null, Void.class),
        HttpStatus.SC_CONFLICT);
  }
}
