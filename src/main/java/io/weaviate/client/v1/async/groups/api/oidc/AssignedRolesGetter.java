package io.weaviate.client.v1.async.groups.api.oidc;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.rbac.model.Role;

public class AssignedRolesGetter extends AsyncBaseClient<List<Role>> implements AsyncClientResult<List<Role>> {
  private String groupId;
  private boolean includePermissions = false;

  public AssignedRolesGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public AssignedRolesGetter withGroupId(String id) {
    this.groupId = id;
    return this;
  }

  public AssignedRolesGetter includePermissions(boolean include) {
    this.includePermissions = include;
    return this;
  }

  private String encodeGroupId() {
    return UrlEncoder.encode(this.groupId);
  }

  @Override
  public Future<Result<List<Role>>> run(FutureCallback<Result<List<Role>>> callback) {
    return sendGetRequest(path(), callback, Result.arrayToListParser(WeaviateRole[].class, WeaviateRole::toRole));
  }

  private String path() {
    return String.format("/authz/groups/%s/roles/oidc?includeFullRoles=%s", encodeGroupId(), includePermissions);
  }
}
