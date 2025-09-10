package io.weaviate.client.v1.groups;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;

public class Groups {
  private final Config config;
  private final HttpClient httpClient;

  public Groups(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public OidcGroups oidc() {
    return new OidcGroups(httpClient, config);
  }
}
