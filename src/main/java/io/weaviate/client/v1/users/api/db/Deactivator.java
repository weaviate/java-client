package io.weaviate.client.v1.users.api.db;

import org.apache.hc.core5.http.HttpStatus;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import lombok.AllArgsConstructor;

public class Deactivator extends BaseClient<Void> implements ClientResult<Boolean> {
  private String userId;
  private boolean revokeKey = false;

  public Deactivator(HttpClient httpClient, Config config) {
    super(httpClient, config);
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
  public Result<Boolean> run() {
    return Result.voidToBoolean(sendPostRequest("/users/db/" + userId + "/deactivate", new Body(revokeKey), Void.class),
        HttpStatus.SC_CONFLICT);
  }
}
