package io.weaviate.client.v1.async.users.api.db;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.AllArgsConstructor;

public class Deactivator extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String userId;
  private boolean revokeKey = false;

  public Deactivator(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public Deactivator withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public Deactivator revokeKey() {
    return revokeKey(true);
  }

  public Deactivator revokeKey(boolean revoke) {
    this.revokeKey = revoke;
    return this;
  }

  @AllArgsConstructor
  private class Body {
    @SerializedName("revoke_key")
    private boolean revokeKey;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest("/users/db/" + userId + "/deactivate", new Body(revokeKey), callback,
        Result.voidToBooleanParser(HttpStatus.SC_CONFLICT));
  }
}
