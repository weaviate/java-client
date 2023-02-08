package technology.semi.weaviate.client.v1.auth;

import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.auth.exception.AuthException;
import technology.semi.weaviate.client.v1.auth.nimbus.AuthType;
import technology.semi.weaviate.client.v1.auth.nimbus.NimbusAuth;
import technology.semi.weaviate.client.v1.auth.provider.AccessTokenProvider;

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
