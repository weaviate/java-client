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
import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.rbac.model.Permission;

public class RoleCreator extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String name;
  private List<Permission<?>> permissions = new ArrayList<>();

  public RoleCreator(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public RoleCreator withName(String name) {
    this.name = name;
    return this;
  }

  public RoleCreator withPermissions(Permission<?>... permissions) {
    this.permissions = Arrays.asList(permissions);
    return this;
  }

  public RoleCreator withPermissions(Permission<?>[]... permissions) {
    List<Permission<?>> all = new ArrayList<>();
    for (Permission<?>[] perm : permissions) {
      all.addAll(Arrays.asList(perm));
    }
    this.permissions = all;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    WeaviateRole role = new WeaviateRole(this.name, this.permissions);
    return sendPostRequest("/authz/roles", role, callback, Result.voidToBooleanParser());
  }
}
