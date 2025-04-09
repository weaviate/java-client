package io.weaviate.client.v1.async.users;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.users.api.RoleAssigner;
import io.weaviate.client.v1.async.users.api.RoleRevoker;
import io.weaviate.client.v1.async.users.api.common.AssignedRolesGetter;
import io.weaviate.client.v1.async.users.api.db.Activator;
import io.weaviate.client.v1.async.users.api.db.AllGetter;
import io.weaviate.client.v1.async.users.api.db.ByNameGetter;
import io.weaviate.client.v1.async.users.api.db.Creator;
import io.weaviate.client.v1.async.users.api.db.Deactivator;
import io.weaviate.client.v1.async.users.api.db.Deleter;
import io.weaviate.client.v1.async.users.api.db.KeyRotator;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbUsers {
  private static final String USER_TYPE = "db";

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

  /** Get roles assigned to a user. */
  public AssignedRolesGetter userRolesGetter() {
    return new AssignedRolesGetter(client, config, tokenProvider, USER_TYPE);
  }

  /** Create a new user. Returns API key for the user to authenticate by. */
  public Creator creator() {
    return new Creator(client, config, tokenProvider);
  }

  /**
   * Delete user.
   * Users declared in the server environment config cannot be
   * deleted ('db_env_user').
   */
  public Deleter deleter() {
    return new Deleter(client, config, tokenProvider);
  }

  /** Activate user account. */
  public Activator activator() {
    return new Activator(client, config, tokenProvider);
  }

  /** Deactivate user account, optionally revoking its API key. */
  public Deactivator deactivator() {
    return new Deactivator(client, config, tokenProvider);
  }

  /** Rotate user's API key. The old key will become invalid. */
  public KeyRotator keyRotator() {
    return new KeyRotator(client, config, tokenProvider);
  }

  /** Get information about the user. */
  public ByNameGetter getUser() {
    return new ByNameGetter(client, config, tokenProvider);
  }

  /** List all known (non-OIDC) users. */
  public AllGetter allGetter() {
    return new AllGetter(client, config, tokenProvider);
  }
}
