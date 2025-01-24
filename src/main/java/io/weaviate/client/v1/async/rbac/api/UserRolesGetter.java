package io.weaviate.client.v1.async.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.rbac.model.Role;

public class UserRolesGetter extends AsyncBaseClient<List<Role>> implements AsyncClientResult<List<Role>> {
  private String user;

  public UserRolesGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  /** Leave unset to fetch roles assigned to the current user. */
  public UserRolesGetter withUser(String user) {
    this.user = user;
    return this;
  }

  @Override
  public Future<Result<List<Role>>> run(FutureCallback<Result<List<Role>>> callback) {
    String path = this.user == null ? "/authz/users/own-roles" : this.path();
    return sendGetRequest(path, callback, new ResponseParser<List<Role>>() {
      @Override
      public Result<List<Role>> parse(HttpResponse response, String body, ContentType contentType) {
        Response<WeaviateRole[]> resp = this.serializer.toResponse(response.getCode(), body, WeaviateRole[].class);
        List<Role> roles = Optional.ofNullable(resp.getBody())
            .map(Arrays::asList)
            .orElse(new ArrayList<>())
            .stream()
            .map(w -> w.toRole())
            .toList();
        return new Result<>(resp.getStatusCode(), roles, resp.getErrors());
      }
    });
  }

  private String path() {
    return String.format("/authz/users/%s/roles", this.user);
  }
}
