package io.weaviate.client.v1.users;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.users.api.MyUserGetter;
import io.weaviate.client.v1.users.api.RoleAssigner;
import io.weaviate.client.v1.users.api.RoleRevoker;
import io.weaviate.client.v1.users.api.UserRolesGetter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Users {

  private final HttpClient httpClient;
  private final Config config;

  /** Get information about the current user. */
  public MyUserGetter myUserGetter() {
    return new MyUserGetter(httpClient, config);
  };

  /** Get roles assigned to a user. */
  public UserRolesGetter userRolesGetter() {
    return new UserRolesGetter(httpClient, config);
  };

  /** Assign a role to a user. Note that 'root' cannot be assigned. */
  public RoleAssigner assigner() {
    return new RoleAssigner(httpClient, config);
  }

  /** Revoke a role from a user. Note that 'root' cannot be revoked. */
  public RoleRevoker revoker() {
    return new RoleRevoker(httpClient, config);
  }
}
