package io.weaviate.client.v1.async.users.api;

import java.util.Optional;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.users.api.WeaviateUser;
import io.weaviate.client.v1.users.model.User;

public class MyUserGetter extends AsyncBaseClient<User> implements AsyncClientResult<User> {
  public MyUserGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  @Override
  public Future<Result<User>> run(FutureCallback<Result<User>> callback) {
    return sendGetRequest("/users/own-info", callback, new ResponseParser<User>() {
      @Override
      public Result<User> parse(HttpResponse response, String body, ContentType contentType) {
        Response<WeaviateUser> resp = this.serializer.toResponse(response.getCode(), body, WeaviateUser.class);
        User user = Optional.ofNullable(resp.getBody())
            .map(WeaviateUser::toUser)
            .orElse(null);
        return new Result<>(resp.getStatusCode(), user, resp.getErrors());
      }
    });
  }
}
