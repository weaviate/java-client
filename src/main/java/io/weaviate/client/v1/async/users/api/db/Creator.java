package io.weaviate.client.v1.async.users.api.db;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

/** Creates a new 'db' user and returns its API key. */
public class Creator extends AsyncBaseClient<String> implements AsyncClientResult<String> {
  private String userId;

  public Creator(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public Creator withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public Future<Result<String>> run(FutureCallback<Result<String>> callback) {
    return sendPostRequest("/users/db/" + userId, null, callback, new ResponseParser<String>() {
      class ApiKey {
        @SerializedName("apikey")
        String apiKey;
      }

      @Override
      public Result<String> parse(HttpResponse response, String body, ContentType contentType) {
        Response<ApiKey> resp = serializer.toResponse(response.getCode(), body, ApiKey.class);
        return new Result<>(resp, resp.getBody() != null ? resp.getBody().apiKey : null);
      }
    });
  }
}
