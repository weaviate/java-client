package io.weaviate.client.v1.users.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.rbac.model.Role;

public class AssignedRolesGetter extends BaseClient<WeaviateRole[]> implements ClientResult<List<Role>> {
  private String userId;
  private boolean includePermissions = false;
  private final String userType;

  public AssignedRolesGetter(HttpClient httpClient, Config config, String userType) {
    super(httpClient, config);
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
  public Result<List<Role>> run() {
    Response<WeaviateRole[]> resp = sendGetRequest(path(), WeaviateRole[].class);
    List<Role> roles = Optional.ofNullable(resp.getBody())
        .map(Arrays::asList).orElse(new ArrayList<>())
        .stream().map(WeaviateRole::toRole)
        .collect(Collectors.toList());
    return new Result<>(resp.getStatusCode(), roles, resp.getErrors());

  }

  private String path() {
    return String.format("/authz/users/%s/roles/%s?includeFullRoles=%s",
        userId, userType, includePermissions);
  }
}
