package technology.semi.weaviate.client.v1.auth;

import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.auth.provider.AuthException;
import technology.semi.weaviate.client.v1.auth.provider.AuthType;
import technology.semi.weaviate.client.v1.auth.provider.NimbusAuth;

public class ClientCredentialsFlow extends NimbusAuth implements Authentication {
  private final String clientSecret;

  public ClientCredentialsFlow(String clientSecret) {
    super();
    this.clientSecret = clientSecret;
  }

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    WeaviateClient authClient = getAuthClient(config, clientSecret, "", "", scopes, AuthType.CLIENT_CREDENTIALS);
    return authClient;
  }

  @Override
  public WeaviateClient getAuthClient(Config config) throws AuthException {
    return getAuthClient(config, null);
  }
}
