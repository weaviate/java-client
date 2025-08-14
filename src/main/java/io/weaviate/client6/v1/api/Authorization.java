package io.weaviate.client6.v1.api;

import java.util.List;

import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.oidc.OidcConfig;
import io.weaviate.client6.v1.internal.oidc.OidcUtils;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public interface Authorization {
  TokenProvider getTokenProvider(RestTransport transport);

  /**
   * Authorize using a static API key.
   *
   * @param apiKey Weaviate API key.
   */
  public static Authorization apiKey(String apiKey) {
    return __ -> TokenProvider.staticToken(apiKey);
  }

  /**
   * Authorize using an existing access_token + refresh_token
   * pair.
   *
   * @param accessToken  Access token.
   * @param refreshToken Refresh token.
   * @param expiresIn    Remaining token lifetime in seconds.
   *
   * @return Authorization provider.
   * @throws WeaviateOAuthException if an error occurred at any point of the
   *                                exchange process.
   */
  public static Authorization bearerToken(String accessToken, String refreshToken, long expiresIn) {
    return transport -> {
      OidcConfig oidc = OidcUtils.getConfig(transport);
      return TokenProvider.bearerToken(oidc, accessToken, refreshToken, expiresIn);
    };
  }

  /**
   * Authorize using Resource Owner Password authorization grant.
   *
   * @param username Resource owner username.
   * @param password Resource owner password.
   * @param scopes   Client scopes.
   *
   * @return Authorization provider.
   * @throws WeaviateOAuthException if an error occured at any point of the token
   *                                exchange process.
   */
  public static Authorization resourceOwnerPassword(String username, String password, List<String> scopes) {
    return transport -> {
      OidcConfig oidc = OidcUtils.getConfig(transport).withScopes(scopes).withScopes("offline_access");
      return TokenProvider.resourceOwnerPassword(oidc, username, password);
    };
  }

  /**
   * Authorize using Client Credentials authorization grant.
   *
   * @param clientId     Client ID.
   * @param clientSecret Client secret.
   * @param scopes       Client scopes.
   *
   * @return Authorization provider.
   * @throws WeaviateOAuthException if an error occured at any point while
   *                                obtaining a new token.
   */
  public static Authorization clientCredentials(String clientId, String clientSecret, List<String> scopes) {
    return transport -> {
      OidcConfig oidc = OidcUtils.getConfig(transport).withScopes(scopes);
      if (oidc.scopes().isEmpty() && TokenProvider.isMicrosoft(oidc)) {
        oidc = oidc.withScopes(clientId + "/.default");
      }
      return TokenProvider.clientCredentials(oidc, clientId, clientSecret);
    };
  }
}
