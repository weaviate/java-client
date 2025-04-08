package io.weaviate.client.v1.async.users.api.common;

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
import io.weaviate.client.v1.rbac.model.Role;

public class AssignedRolesGetter extends AsyncBaseClient<List<Role>> implements AsyncClientResult<List<Role>> {
  private String userId;
  private boolean includePermissions = false;

  private final String userType;

  public AssignedRolesGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider,
      String userType) {
    super(httpClient, config, tokenProvider);
    this.userType = userType;
  }

  public AssignedRolesGetter withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Include a full list of permissions for each role.
   * If not set, only role names will be populated.
   */
  public AssignedRolesGetter includePermissions() {
    return includePermissions(true);
  }

  public AssignedRolesGetter includePermissions(boolean include) {
    this.includePermissions = include;
    return this;
  }

  @Override
  public Future<Result<List<Role>>> run(FutureCallback<Result<List<Role>>> callback) {
    return sendGetRequest(path(), callback, Result.arrayToListParser(WeaviateRole[].class, WeaviateRole::toRole));
  }

  private String path() {
    return String.format("/authz/users/%s/roles/%s?includeFullRoles=%s",
        userId, userType, includePermissions);
  }
}
