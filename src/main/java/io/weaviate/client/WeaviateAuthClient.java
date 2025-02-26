package io.weaviate.client;

import java.util.List;

import io.weaviate.client.v1.auth.ApiKeyFlow;
import io.weaviate.client.v1.auth.BearerTokenFlow;
import io.weaviate.client.v1.auth.ClientCredentialsFlow;
import io.weaviate.client.v1.auth.ResourceOwnerPasswordFlow;
import io.weaviate.client.v1.auth.exception.AuthException;

public class WeaviateAuthClient {

  public static WeaviateClient clientCredentials(Config config, String clientSecret, List<String> scopes)
      throws AuthException {
    ClientCredentialsFlow clientCredentialsFlow = new ClientCredentialsFlow(clientSecret);
    return clientCredentialsFlow.getAuthClient(config, scopes);
  }

  public static WeaviateClient clientPassword(Config config, String username, String password, List<String> scopes)
      throws AuthException {
    ResourceOwnerPasswordFlow resourceOwnerPasswordFlow = new ResourceOwnerPasswordFlow(username, password);
    return resourceOwnerPasswordFlow.getAuthClient(config, scopes);
  }

  public static WeaviateClient bearerToken(Config config, String accessToken, long accessTokenLifetime,
      String refreshToken) throws AuthException {
    BearerTokenFlow bearerTokenFlow = new BearerTokenFlow(accessToken, accessTokenLifetime, refreshToken);
    return bearerTokenFlow.getAuthClient(config, null);
  }

  /**
   * apiKey returns a new WeaviateClient with ApiKey token provider.
   * If the host is a Weaviate domain, it also adds headers necessary
   * for authenticating to Weaviate Embeddings service.
   */
  public static WeaviateClient apiKey(Config config, String apiKey) throws AuthException {
    ApiKeyFlow flow = new ApiKeyFlow(apiKey);
    if (isWeaviateDomain(config.getHost())) {
      addWeaviateHeaders(config, apiKey);
    }
    return flow.getAuthClient(config);
  }

  private static final String HEADER_X_WEAVIATE_API_KEY = "X-Weaviate-Api-Key";
  private static final String HEADER_X_WEAVIATE_CLUSTER_URL = "X-Weaviate-Cluster-URL";

  /**
   * addWeaviateHeaders sets headers necessary for authenticating
   * with Weaviate Embedding service.
   */
  private static void addWeaviateHeaders(Config config, String apiKey) {
    config.setHeader(HEADER_X_WEAVIATE_API_KEY, apiKey);
    config.setHeader(HEADER_X_WEAVIATE_CLUSTER_URL, "https://" + config.getHost());
  }

  /**
   * isWeaviateDomain returns true if the host matches weaviate.io,
   * semi.technology, or weaviate.cloud domain.
   */
  private static boolean isWeaviateDomain(String host) {
    String lower = host.toLowerCase();
    return lower.contains("weaviate.io") ||
        lower.contains("semi.technology") ||
        lower.contains("weaviate.cloud");
  }
}
