package io.weaviate.client.v1.groups.api.oidc;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.rbac.model.Role;

public class AssignedRolesGetter extends BaseClient<WeaviateRole[]> implements ClientResult<List<Role>> {
  private String groupId;
  private boolean includePermissions = false;

  public AssignedRolesGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public AssignedRolesGetter withGroupId(String id) {
    this.groupId = id;
    return this;
  }

  public AssignedRolesGetter includePermissions(boolean include) {
    this.includePermissions = include;
    return this;
  }

  private String _groupId() {
    return UrlEncoder.encode(this.groupId);
  }

  @Override
  public Result<List<Role>> run() {
    return Result.toList(sendGetRequest(path(), WeaviateRole[].class), WeaviateRole::toRole);
  }

  private String path() {
    return String.format("/authz/groups/%s/roles/oidc?includeFullRoles=%s", _groupId(), includePermissions);
  }
}
