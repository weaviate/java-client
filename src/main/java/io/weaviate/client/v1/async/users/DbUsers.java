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

  public AssignedRolesGetter assignedRoles() {
    return new AssignedRolesGetter(client, config, tokenProvider, USER_TYPE);
  }

  public Creator creator() {
    return new Creator(client, config, tokenProvider);
  }

  public Deleter deleter() {
    return new Deleter(client, config, tokenProvider);
  }

  public Activator activator() {
    return new Activator(client, config, tokenProvider);
  }

  public Deactivator deactivator() {
    return new Deactivator(client, config, tokenProvider);
  }

  public KeyRotator keyRotator() {
    return new KeyRotator(client, config, tokenProvider);
  }

  public ByNameGetter getUser() {
    return new ByNameGetter(client, config, tokenProvider);
  }

  public AllGetter allGetter() {
    return new AllGetter(client, config, tokenProvider);
  }
}
