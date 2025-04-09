package io.weaviate.client.v1.async.users.api.db;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.users.model.UserDb;

public class AllGetter extends AsyncBaseClient<List<UserDb>> implements AsyncClientResult<List<UserDb>> {

  public AllGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  @Override
  public Future<Result<List<UserDb>>> run(FutureCallback<Result<List<UserDb>>> callback) {
    return sendGetRequest("/users/db", callback, Result.arrayToListParser(UserDb[].class));
  }
}
