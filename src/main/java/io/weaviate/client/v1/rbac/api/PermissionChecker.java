package io.weaviate.client.v1.rbac.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.model.Permission;

public class PermissionChecker extends BaseClient<Boolean> implements ClientResult<Boolean> {
  private String role;
  private Permission<?> permission;

  public PermissionChecker(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public PermissionChecker withRole(String role) {
    this.role = role;
    return this;
  }

  public PermissionChecker withPermission(Permission<?> permission) {
    this.permission = permission;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    return new Result<Boolean>(sendPostRequest(path(), permission.toWeaviate(), Boolean.class));
  }

  private String path() {
    return String.format("/authz/roles/%s/has-permission", this.role);
  }
}
