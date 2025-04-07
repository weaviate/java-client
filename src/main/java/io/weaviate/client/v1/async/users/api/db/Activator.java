package io.weaviate.client.v1.async.users.api.db;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class Activator extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String userId;

  public Activator(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public Activator withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest("/users/db/" + userId + "/activate", null, callback,
        Result.voidToBooleanParser(HttpStatus.SC_CONFLICT));
  }
}
