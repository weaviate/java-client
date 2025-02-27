package io.weaviate.client.v1.async.rbac.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.rbac.model.Permission;

public class PermissionChecker extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String role;
  private Permission<?> permission;

  public PermissionChecker(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
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
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest(path(), permission.firstToWeaviate(), Boolean.class, callback);
  }

  private String path() {
    return String.format("/authz/roles/%s/has-permission", this.role);
  }
}
