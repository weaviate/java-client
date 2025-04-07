package io.weaviate.client.v1.async.users;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.users.api.RoleAssigner;
import io.weaviate.client.v1.async.users.api.RoleRevoker;
import io.weaviate.client.v1.async.users.api.common.AssignedRolesGetter;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OidcUsers {
  private static final String USER_TYPE = "oidc";

  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  /** Assign a role to a user. Note that 'root' cannot be assigned. */
  public RoleAssigner assigner() {
    return new RoleAssigner(client, config, tokenProvider, USER_TYPE);
  }

  /** Revoke a role from a user. Note that 'root' cannot be revoked. */
  public RoleRevoker revoker() {
    return new RoleRevoker(client, config, tokenProvider, USER_TYPE);
  }

  public AssignedRolesGetter assignedRoles() {
    return new AssignedRolesGetter(client, config, tokenProvider, USER_TYPE);
  }
}
