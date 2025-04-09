package io.weaviate.client.v1.users.api.db;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.api.db.Creator.ApiKey;

/** Creates a new 'db' user and returns its API key. */
public class Creator extends BaseClient<ApiKey> implements ClientResult<String> {
  private String userId;

  public Creator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public Creator withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  static class ApiKey {
    @SerializedName("apikey")
    String apiKey;
  }

  @Override
  public Result<String> run() {
    Response<ApiKey> resp = sendPostRequest("/users/db/" + userId, null, ApiKey.class);
    return new Result<>(resp, resp.getBody() != null ? resp.getBody().apiKey : null);
  }
}
