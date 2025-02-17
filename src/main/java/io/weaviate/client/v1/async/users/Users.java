package io.weaviate.client.v1.async.users;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
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

  /** Get roles assigned to a user. */
  public UserRolesGetter userRolesGetter() {
    return new UserRolesGetter(client, config, tokenProvider);
  };

  public RoleAssigner assigner() {
    return new RoleAssigner(client, config, tokenProvider);
  }

  public RoleRevoker revoker() {
    return new RoleRevoker(client, config, tokenProvider);
  }
}
