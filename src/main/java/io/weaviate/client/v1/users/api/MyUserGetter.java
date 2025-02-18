package io.weaviate.client.v1.users.api;

import java.util.Optional;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.model.User;

public class MyUserGetter extends BaseClient<WeaviateUser> implements ClientResult<User> {
  public MyUserGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<User> run() {
    Response<WeaviateUser> resp = sendGetRequest("/users/own-info", WeaviateUser.class);
    User user = Optional.ofNullable(resp.getBody()).map(WeaviateUser::toUser).orElse(null);
    return new Result<>(resp.getStatusCode(), user, resp.getErrors());
  }
}
