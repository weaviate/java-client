package io.weaviate.client.v1.aliases.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class AliasDeleter extends BaseClient<Void> implements ClientResult<Boolean> {
  private String alias;

  public AliasDeleter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public AliasDeleter withAlias(String alias) {
    this.alias = alias;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    Response<Void> resp = sendDeleteRequest("/aliases/" + alias, null, Void.class);
    return Result.voidToBoolean(resp);
  }
}
