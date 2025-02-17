package io.weaviate.client.v1.users.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import lombok.AllArgsConstructor;

public class RoleAssigner extends BaseClient<Void> implements ClientResult<Boolean> {
  private String user;
  private List<String> roles = new ArrayList<>();

  public RoleAssigner(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public RoleAssigner withUser(String user) {
    this.user = user;
    return this;
  }

  public RoleAssigner witRoles(String... roles) {
    this.roles = Arrays.asList(roles);
    return this;
  }

  /** The API signature for this method is { "roles": [...] } */
  @AllArgsConstructor
  private static class Body {
    public final List<String> roles;
  }

  @Override
  public Result<Boolean> run() {
    Response<Void> resp = sendPostRequest(path(), new Body(this.roles), Void.class);
    int status = resp.getStatusCode();
    return new Result<Boolean>(status, status == 200, resp.getErrors());
  }

  private String path() {
    return String.format("/authz/users/%s/assign", this.user);
  }
}
