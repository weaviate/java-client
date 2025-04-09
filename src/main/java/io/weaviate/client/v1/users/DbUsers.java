package io.weaviate.client.v1.users;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.api.RoleAssigner;
import io.weaviate.client.v1.users.api.RoleRevoker;
import io.weaviate.client.v1.users.api.common.AssignedRolesGetter;
import io.weaviate.client.v1.users.api.db.Activator;
import io.weaviate.client.v1.users.api.db.AllGetter;
import io.weaviate.client.v1.users.api.db.ByNameGetter;
import io.weaviate.client.v1.users.api.db.Creator;
import io.weaviate.client.v1.users.api.db.Deactivator;
import io.weaviate.client.v1.users.api.db.Deleter;
import io.weaviate.client.v1.users.api.db.KeyRotator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbUsers {
  private static final String USER_TYPE = "db";

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

  /** Get roles assigned to a user. */
  public AssignedRolesGetter userRolesGetter() {
    return new AssignedRolesGetter(httpClient, config, USER_TYPE);
  }

  /** Create a new user. Returns API key for the user to authenticate by. */
  public Creator creator() {
    return new Creator(httpClient, config);
  }

  /**
   * Delete user.
   * Users declared in the server environment config cannot be
   * deleted ('db_env_user').
   */
  public Deleter deleter() {
    return new Deleter(httpClient, config);
  }

  /** Activate user account. */
  public Activator activator() {
    return new Activator(httpClient, config);
  }

  /** Deactivate user account, optionally revoking its API key. */
  public Deactivator deactivator() {
    return new Deactivator(httpClient, config);
  }

  /** Rotate user's API key. The old key will become invalid. */
  public KeyRotator keyRotator() {
    return new KeyRotator(httpClient, config);
  }

  /** Get information about the user. */
  public ByNameGetter getUser() {
    return new ByNameGetter(httpClient, config);
  }

  /** List all known (non-OIDC) users. */
  public AllGetter allGetter() {
    return new AllGetter(httpClient, config);
  }
}
