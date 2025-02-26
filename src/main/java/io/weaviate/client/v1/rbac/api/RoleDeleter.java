package io.weaviate.client.v1.rbac.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class RoleDeleter extends BaseClient<Void> implements ClientResult<Boolean> {
  private String name;

  public RoleDeleter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public RoleDeleter withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    return Result.voidToBoolean(sendDeleteRequest("/authz/roles/" + this.name, null, Void.class));
  }
}
