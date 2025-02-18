package io.weaviate.client.v1.users.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import lombok.AllArgsConstructor;

public class RoleRevoker extends BaseClient<Void> implements ClientResult<Boolean> {
  private String user;
  private List<String> roles = new ArrayList<>();

  public RoleRevoker(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public RoleRevoker withUser(String user) {
    this.user = user;
    return this;
  }

  public RoleRevoker witRoles(String... roles) {
    this.roles = Collections.unmodifiableList(Arrays.asList(roles));
    return this;
  }

  /** The API signature for this method is { "roles": [...] } */
  @AllArgsConstructor
  private static class Body {
    public final List<String> roles;
  }

  @Override
  public Result<Boolean> run() {
    return Result.voidToBoolean(sendPostRequest(path(), new Body(this.roles), Void.class));
  }

  private String path() {
    return String.format("/authz/users/%s/revoke", this.user);
  }
}
