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

public class AliasGetter extends AsyncBaseClient<Alias> implements AsyncClientResult<Alias> {
  private String alias;

  public AliasGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public AliasGetter withAlias(String alias) {
    this.alias = alias;
    return this;
  }

  @Override
  public Future<Result<Alias>> run(FutureCallback<Result<Alias>> callback) {
    return sendGetRequest("/aliases/" + alias, Alias.class, callback);
  }
}
