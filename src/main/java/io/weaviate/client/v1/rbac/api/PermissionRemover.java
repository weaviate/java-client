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

public class PermissionRemover extends BaseClient<Void> implements ClientResult<Void> {
  private String name;
  private List<Permission<?>> permissions = new ArrayList<>();

  public PermissionRemover(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public PermissionRemover withName(String name) {
    this.name = name;
    return this;
  }

  public PermissionRemover withPermissions(Permission<?>... permissions) {
    this.permissions = Arrays.asList(permissions);
    return this;
  }

  @Override
  public Result<Void> run() {
    List<WeaviatePermission> permissions = WeaviatePermission.mergePermissions(this.permissions);
    return new Result<Void>(sendPostRequest(path(), permissions, Void.class));
  }

  private String path() {
    return String.format("/authz/roles/%s/remove-permissions", this.name);
  }
}
