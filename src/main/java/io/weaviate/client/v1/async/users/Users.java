package io.weaviate.client.v1.async.users;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.users.api.MyUserGetter;
import io.weaviate.client.v1.async.users.api.RoleAssigner;
import io.weaviate.client.v1.async.users.api.RoleRevoker;
import io.weaviate.client.v1.async.users.api.UserRolesGetter;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Users {

  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  /** Get information about the current user. */
  public MyUserGetter myUserGetter() {
    return new MyUserGetter(client, config, tokenProvider);
  };

  /**
   * Get roles assigned to a user.
   * <p>
   * Deprecated - prefer {@link DbUsers#userRolesGetter()} or
   * {@link OidcUsers#userRolesGetter()}.
   */
  @Deprecated
  public UserRolesGetter userRolesGetter() {
    return new UserRolesGetter(client, config, tokenProvider);
  };

  /**
   * Assign a role to a user. Note that 'root' cannot be assigned.
   * <p>
   * Deprecated - prefer {@link DbUsers#assigner()} or
   * {@link OidcUsers#assigner()}.
   */
  @Deprecated
  public RoleAssigner assigner() {
    return new RoleAssigner(client, config, tokenProvider);
  }

  /**
   * Revoke a role from a user. Note that 'root' cannot be revoked.
   * <p>
   * Deprecated - prefer {@link DbUsers#revoker()} or
   * {@link OidcUsers#revoker()}
   */
  @Deprecated
  public RoleRevoker revoker() {
    return new RoleRevoker(client, config, tokenProvider);
  }

  /** Manage dynamic users, their roles and permissions. */
  public DbUsers db() {
    return new DbUsers(client, config, tokenProvider);
  }

  /** Manage users authenticated via OIDC, their roles and permissions. */
  public OidcUsers oidc() {
    return new OidcUsers(client, config, tokenProvider);
  }
}
