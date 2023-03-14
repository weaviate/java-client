package io.weaviate.client.v1.auth;

import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.auth.nimbus.AuthType;
import io.weaviate.client.v1.auth.nimbus.NimbusAuth;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import java.util.List;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;

public class ClientCredentialsFlow implements Authentication {

  private final NimbusAuth nimbusAuth;
  private final String clientSecret;

  public ClientCredentialsFlow(String clientSecret) {
    this.nimbusAuth = new NimbusAuth();
    this.clientSecret = clientSecret;
  }

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    AccessTokenProvider tokenProvider = nimbusAuth.getAccessTokenProvider(config, clientSecret, "", "", scopes, AuthType.CLIENT_CREDENTIALS);
    return new WeaviateClient(config, tokenProvider);
  }

  @Override
  public WeaviateClient getAuthClient(Config config) throws AuthException {
    return getAuthClient(config, null);
  }
}
