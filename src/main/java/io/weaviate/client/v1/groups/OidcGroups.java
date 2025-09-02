package io.weaviate.client.v1.groups;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.groups.api.oidc.AssignedRolesGetter;
import io.weaviate.client.v1.groups.api.oidc.KnownGroupNamesGetter;
import io.weaviate.client.v1.groups.api.oidc.RoleAssigner;
import io.weaviate.client.v1.groups.api.oidc.RoleRevoker;

public class OidcGroups {
  private final Config config;
  private final HttpClient httpClient;

  public OidcGroups(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public RoleAssigner roleAssigner() {
    return new RoleAssigner(httpClient, config);
  }

  public RoleRevoker roleRevoker() {
    return new RoleRevoker(httpClient, config);
  }

  public AssignedRolesGetter assignedRolesGetter() {
    return new AssignedRolesGetter(httpClient, config);
  }

  public KnownGroupNamesGetter knownGroupNamesGetter() {
    return new KnownGroupNamesGetter(httpClient, config);
  }
}
