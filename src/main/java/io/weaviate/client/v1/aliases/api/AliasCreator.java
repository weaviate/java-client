package io.weaviate.client.v1.aliases.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.aliases.model.Alias;

public class AliasCreator extends BaseClient<Void> implements ClientResult<Boolean> {
  private String className;
  private String alias;

  public AliasCreator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public AliasCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public AliasCreator withAlias(String alias) {
    this.alias = alias;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    Response<Void> resp = sendPostRequest("/aliases", new Alias(className, alias), Void.class);
    return Result.voidToBoolean(resp);
  }
}
