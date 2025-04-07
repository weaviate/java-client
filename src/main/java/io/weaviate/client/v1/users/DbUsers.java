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

  public AssignedRolesGetter assignedRoles() {
    return new AssignedRolesGetter(httpClient, config, USER_TYPE);
  }

  public Creator creator() {
    return new Creator(httpClient, config);
  }

  public Deleter deleter() {
    return new Deleter(httpClient, config);
  }

  public Activator activator() {
    return new Activator(httpClient, config);
  }

  public Deactivator deactivator() {
    return new Deactivator(httpClient, config);
  }

  public KeyRotator keyRotator() {
    return new KeyRotator(httpClient, config);
  }

  public ByNameGetter getUser() {
    return new ByNameGetter(httpClient, config);
  }

  public AllGetter allGetter() {
    return new AllGetter(httpClient, config);
  }
}
