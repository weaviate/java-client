package io.weaviate.client.v1.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.model.Permission;

public class RoleCreator extends BaseClient<Void> implements ClientResult<Void> {
  private String name;
  private List<Permission<?>> permissions = new ArrayList<>();

  public RoleCreator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public RoleCreator withName(String name) {
    this.name = name;
    return this;
  }

  public RoleCreator withPermissions(Permission<?>... permissions) {
    this.permissions = Arrays.asList(permissions);
    return this;
  }

  @Override
  public Result<Void> run() {
    WeaviateRole role = new WeaviateRole(this.name, this.permissions);
    return new Result<Void>(sendPostRequest("/authz/roles", role, Void.class));
  }
}
