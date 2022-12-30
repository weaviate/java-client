package technology.semi.weaviate.client;

import java.util.List;
import technology.semi.weaviate.client.v1.auth.BearerTokenFlow;
import technology.semi.weaviate.client.v1.auth.ClientCredentialsFlow;
import technology.semi.weaviate.client.v1.auth.ResourceOwnerPasswordFlow;
import technology.semi.weaviate.client.v1.auth.provider.AuthException;

public class WeaviateAuthClient {

  public static WeaviateClient clientCredentials(Config config, String clientSecret, List<String> scopes) throws AuthException {
    ClientCredentialsFlow clientCredentialsFlow = new ClientCredentialsFlow(clientSecret);
    return clientCredentialsFlow.getAuthClient(config, scopes);
  }

  public static WeaviateClient clientPassword(Config config, String username, String password, List<String> scopes) throws AuthException {
    ResourceOwnerPasswordFlow resourceOwnerPasswordFlow = new ResourceOwnerPasswordFlow(username, password);
    return resourceOwnerPasswordFlow.getAuthClient(config, scopes);
  }

  public static WeaviateClient bearerToken(Config config, String accessToken, long accessTokenLifetime, String refreshToken) throws AuthException {
    BearerTokenFlow bearerTokenFlow = new BearerTokenFlow(accessToken, accessTokenLifetime, refreshToken);
    return bearerTokenFlow.getAuthClient(config, null);
  }
}
