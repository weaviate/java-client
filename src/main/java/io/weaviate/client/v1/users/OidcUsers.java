package io.weaviate.client.v1.users;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.api.RoleAssigner;
import io.weaviate.client.v1.users.api.RoleRevoker;
import io.weaviate.client.v1.users.api.common.AssignedRolesGetter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OidcUsers {
  private static final String USER_TYPE = "oidc";

  private final HttpClient httpClient;
  private final Config config;

  /** Assign a role to a user. Note that 'root' cannot be assigned. */
  public RoleAssigner assigner() {
    return new RoleAssigner(httpClient, config, USER_TYPE);
  }

  /** Revoke a role from a user. Note that 'root' cannot be revoked. */
  public RoleRevoker revoker() {
    return new RoleRevoker(httpClient, config, USER_TYPE);
  }

  public AssignedRolesGetter assignedRoles() {
    return new AssignedRolesGetter(httpClient, config, USER_TYPE);
  }
}
