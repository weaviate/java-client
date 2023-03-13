package io.weaviate.client;

import io.weaviate.client.v1.auth.BearerTokenFlow;
import io.weaviate.client.v1.auth.ClientCredentialsFlow;
import io.weaviate.client.v1.auth.ResourceOwnerPasswordFlow;
import io.weaviate.client.v1.auth.exception.AuthException;
import java.util.List;

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
