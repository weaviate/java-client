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

public class PermissionRemover extends BaseClient<Void> implements ClientResult<Boolean> {
  private String role;
  private List<Permission<?>> permissions = new ArrayList<>();

  public PermissionRemover(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public PermissionRemover withRole(String role) {
    this.role = role;
    return this;
  }

  public PermissionRemover withPermissions(Permission<?>... permissions) {
    this.permissions = Arrays.asList(permissions);
    return this;
  }

  public PermissionRemover withPermissions(Permission<?>[]... permissions) {
    List<Permission<?>> all = new ArrayList<>();
    for (Permission<?>[] perm : permissions) {
      all.addAll(Arrays.asList(perm));
    }
    this.permissions = all;
    return this;
  }

  @AllArgsConstructor
  private static class Body {
    public final List<?> permissions;
  }

  @Override
  public Result<Boolean> run() {
    List<WeaviatePermission> permissions = WeaviatePermission.mergePermissions(this.permissions);
    return Result.voidToBoolean(sendPostRequest(path(), new Body(permissions), Void.class));
  }

  private String path() {
    return String.format("/authz/roles/%s/remove-permissions", this.role);
  }
}
