package io.weaviate.client.v1.async.groups;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.groups.api.oidc.AssignedRolesGetter;
import io.weaviate.client.v1.async.groups.api.oidc.KnownGroupNamesGetter;
import io.weaviate.client.v1.async.groups.api.oidc.RoleAssigner;
import io.weaviate.client.v1.async.groups.api.oidc.RoleRevoker;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OidcGroups {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  public RoleAssigner roleAssigner() {
    return new RoleAssigner(client, config, tokenProvider);
  }

  public RoleRevoker roleRevoker() {
    return new RoleRevoker(client, config, tokenProvider);
  }

  public AssignedRolesGetter assignedRolesGetter() {
    return new AssignedRolesGetter(client, config, tokenProvider);
  }

  public KnownGroupNamesGetter knownGroupNamesGetter() {
    return new KnownGroupNamesGetter(client, config, tokenProvider);
  }
}
