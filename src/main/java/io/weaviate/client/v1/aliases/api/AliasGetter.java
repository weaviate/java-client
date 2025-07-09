package io.weaviate.client.v1.aliases.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.aliases.model.Alias;

public class AliasGetter extends BaseClient<Alias> implements ClientResult<Alias> {
  private String alias;

  public AliasGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public AliasGetter withAlias(String alias) {
    this.alias = alias;
    return this;
  }

  @Override
  public Result<Alias> run() {
    return new Result<>(sendGetRequest("/aliases/" + alias, Alias.class));
  }
}
