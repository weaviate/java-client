package io.weaviate.client6.v1.api;

import java.util.List;

import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.oidc.OidcConfig;
import io.weaviate.client6.v1.internal.oidc.OidcUtils;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public interface Authentication {
  TokenProvider getTokenProvider(RestTransport transport);

  /**
   * Authenticate using a static API key.
   *
   * @param apiKey Weaviate API key.
   */
  public static Authentication apiKey(String apiKey) {
    return __ -> TokenProvider.staticToken(apiKey);
  }

  /**
   * Authenticate using an existing access_token + refresh_token
   * pair.
   *
   * @param accessToken  Access token.
   * @param refreshToken Refresh token.
   * @param expiresIn    Remaining token lifetime in seconds.
   *
   * @return Authentication provider.
   * @throws WeaviateOAuthException if an error occurred at any point of the
   *                                exchange process.
   */
  public static Authentication bearerToken(String accessToken, String refreshToken, long expiresIn) {
    return transport -> {
      OidcConfig oidc = OidcUtils.getConfig(transport);
      return TokenProvider.bearerToken(oidc, accessToken, refreshToken, expiresIn);
    };
  }

  /**
   * Authenticate using Resource Owner Password authorization grant.
   *
   * @param username Resource owner username.
   * @param password Resource owner password.
   * @param scopes   Client scopes.
   *
   * @return Authentication provider.
   * @throws WeaviateOAuthException if an error occurred at any point of the token
   *                                exchange process.
   */
  public static Authentication resourceOwnerPassword(String username, String password, List<String> scopes) {
    return transport -> {
      OidcConfig oidc = OidcUtils.getConfig(transport).withScopes(scopes).withScopes("offline_access");
      return TokenProvider.resourceOwnerPassword(oidc, username, password);
    };
  }

  /**
   * Authenticate using Client Credentials authorization grant.
   *
   * @param clientSecret Client secret.
   * @param scopes       Client scopes.
   *
   * @return Authentication provider.
   * @throws WeaviateOAuthException if an error occurred at any point while
   *                                obtaining a new token.
   */
  public static Authentication clientCredentials(String clientSecret, List<String> scopes) {
    return transport -> {
      OidcConfig oidc = OidcUtils.getConfig(transport).withScopes(scopes);
      if (oidc.scopes().isEmpty() && TokenProvider.isMicrosoft(oidc)) {
        oidc = oidc.withScopes(oidc.clientId() + "/.default");
      }
      return TokenProvider.clientCredentials(oidc, clientSecret);
    };
  }
}
