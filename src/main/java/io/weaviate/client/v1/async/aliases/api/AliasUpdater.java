package io.weaviate.client.v1.async.aliases.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class AliasUpdater extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String className;
  private String alias;

  public AliasUpdater(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
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
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPutRequest("/aliases/" + alias, new Body(),
        callback, Result.voidToBooleanParser());
  }
}
