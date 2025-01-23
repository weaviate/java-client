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
import lombok.AllArgsConstructor;

public class PermissionAdder extends BaseClient<Void> implements ClientResult<Void> {
  private String role;
  private List<Permission<?>> permissions = new ArrayList<>();

  public PermissionAdder(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public PermissionAdder withRole(String name) {
    this.role = name;
    return this;
  }

  public PermissionAdder withPermissions(Permission<?>... permissions) {
    this.permissions = Arrays.asList(permissions);
    return this;
  }

  @AllArgsConstructor
  private static class Body {
    public final List<?> permissions;
  }

  @Override
  public Result<Void> run() {
    List<WeaviatePermission> permissions = WeaviatePermission.mergePermissions(this.permissions);
    return new Result<Void>(sendPostRequest(path(), new Body(permissions), Void.class));
  }

  private String path() {
    return String.format("/authz/roles/%s/add-permissions", this.role);
  }
}
