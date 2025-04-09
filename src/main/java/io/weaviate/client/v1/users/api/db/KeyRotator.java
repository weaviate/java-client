package io.weaviate.client.v1.users.api.db;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.api.db.KeyRotator.ApiKey;

public class KeyRotator extends BaseClient<ApiKey> implements ClientResult<String> {
  private String userId;

  public KeyRotator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public KeyRotator withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  static class ApiKey {
    @SerializedName("apikey")
    String apiKey;
  }

  @Override
  public Result<String> run() {
    Response<ApiKey> resp = sendPostRequest("/users/db/" + userId + "/rotate-key", null, ApiKey.class);
    return new Result<>(resp, resp.getBody().apiKey);
  }
}
