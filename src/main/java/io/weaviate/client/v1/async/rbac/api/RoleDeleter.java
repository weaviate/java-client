package io.weaviate.client.v1.async.rbac.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class RoleDeleter extends AsyncBaseClient<Void> implements AsyncClientResult<Void> {
  private String name;

  public RoleDeleter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public RoleDeleter withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Future<Result<Void>> run(FutureCallback<Result<Void>> callback) {
    return sendDeleteRequest("/authz/roles/" + this.name, null, Void.class, callback);
  }
}
