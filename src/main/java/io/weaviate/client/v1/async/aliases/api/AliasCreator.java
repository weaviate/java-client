package io.weaviate.client.v1.async.aliases.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.aliases.model.Alias;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class AliasCreator extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String className;
  private String alias;

  public AliasCreator(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
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
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest("/aliases", new Alias(className, alias),
        callback, Result.voidToBooleanParser());
  }
}
