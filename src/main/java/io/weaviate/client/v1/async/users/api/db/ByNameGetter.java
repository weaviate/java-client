package io.weaviate.client.v1.async.users.api.db;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.users.model.UserDb;

public class ByNameGetter extends AsyncBaseClient<UserDb> implements AsyncClientResult<UserDb> {
  private String userId;

  public ByNameGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public ByNameGetter withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public Future<Result<UserDb>> run(FutureCallback<Result<UserDb>> callback) {
    return sendGetRequest("/users/db/" + userId, UserDb.class, callback);
  }
}
