package io.weaviate.client.v1.aliases.api;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class AliasUpdater extends BaseClient<Void> implements ClientResult<Boolean> {
  private String className;
  private String alias;

  public AliasUpdater(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public AliasUpdater withAlias(String alias) {
    this.alias = alias;
    return this;
  }

  public AliasUpdater withNewClassName(String className) {
    this.className = className;
    return this;
  }

  class Body {
    @SerializedName("class")
    String className = AliasUpdater.this.className;
  }

  @Override
  public Result<Boolean> run() {
    Response<Void> resp = sendPutRequest("/aliases/" + alias, new Body(), Void.class);
    return Result.voidToBoolean(resp);
  }
}
