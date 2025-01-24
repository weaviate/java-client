package io.weaviate.client.v1.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.model.Role;

public class UserRolesGetter extends BaseClient<WeaviateRole[]> implements ClientResult<List<Role>> {
  private String user;

  public UserRolesGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  /** Leave unset to fetch roles assigned to the current user. */
  public UserRolesGetter withUser(String user) {
    this.user = user;
    return this;
  }

  @Override
  public Result<List<Role>> run() {
    String path = this.user == null ? "/authz/users/own-roles" : this.path();
    Response<WeaviateRole[]> resp = sendGetRequest(path, WeaviateRole[].class);
    List<Role> roles = Optional.ofNullable(resp.getBody())
        .map(Arrays::asList)
        .orElse(new ArrayList<>())
        .stream()
        .map(w -> w.toRole())
        .toList();
    return new Result<>(resp.getStatusCode(), roles, resp.getErrors());
  }

  private String path() {
    return String.format("/authz/users/%s/roles", this.user);
  }
}
