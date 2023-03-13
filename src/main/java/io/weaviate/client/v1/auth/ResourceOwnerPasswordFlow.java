package io.weaviate.client.v1.auth;

import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.auth.nimbus.AuthType;
import io.weaviate.client.v1.auth.nimbus.NimbusAuth;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import java.util.ArrayList;
import java.util.List;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;

public class ResourceOwnerPasswordFlow implements Authentication {

  private final NimbusAuth nimbusAuth;
  private final String username;
  private final String password;

  public ResourceOwnerPasswordFlow(String username, String password) {
    this.nimbusAuth = new NimbusAuth();
    this.username = username;
    this.password = password;
  }

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    AccessTokenProvider tokenProvider = nimbusAuth.getAccessTokenProvider(config, "", username, password, addDefaultScopes(scopes), AuthType.USER_PASSWORD);
    return new WeaviateClient(config, tokenProvider);
  }

  @Override
  public WeaviateClient getAuthClient(Config config) throws AuthException {
    return getAuthClient(config, null);
  }

  private List<String> addDefaultScopes(List<String> scopes) {
    List<String> withDefaultScopes = new ArrayList<>();
    withDefaultScopes.add("offline_access");
    if (scopes != null) {
      scopes.forEach(withDefaultScopes::add);
    }
    return withDefaultScopes;
  }
}
