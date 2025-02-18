package io.weaviate.client.v1.async.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import io.weaviate.client.v1.rbac.model.Permission;
import lombok.AllArgsConstructor;

public class PermissionRemover extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String role;
  private List<Permission<?>> permissions = new ArrayList<>();

  public PermissionRemover(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
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

  /** The API signature for this method is { "permissions": [...] } */
  @AllArgsConstructor
  private static class Body {
    public final List<?> permissions;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    List<WeaviatePermission> permissions = WeaviatePermission.mergePermissions(this.permissions);
    return sendPostRequest(path(), new Body(permissions), callback, Result.voidToBooleanParser());
  }

  private String path() {
    return String.format("/authz/roles/%s/remove-permissions", this.role);
  }
}
