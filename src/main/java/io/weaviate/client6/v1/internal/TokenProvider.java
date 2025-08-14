package io.weaviate.client6.v1.internal;

import java.net.URI;
import java.time.Instant;

import io.weaviate.client6.v1.api.WeaviateOAuthException;
import io.weaviate.client6.v1.internal.oidc.OidcConfig;
import io.weaviate.client6.v1.internal.oidc.nimbus.NimbusTokenProvider;

/** TokenProvider obtains authentication tokens. */
@FunctionalInterface
public interface TokenProvider {
  Token getToken();

  /** Token represents an access_token + refresh_token pair. */
  public record Token(String accessToken, String refreshToken, Instant createdAt, long expiresIn) {
    /**
     * Returns {@code true} if remaining lifetime of the token is greater than 0.
     * Tokens created with {@link #expireNever} are always valid.
     */
    public boolean isValid() {
      if (expiresIn == -1) {
        return true;
      }
      // TODO: adjust for expireDelta
      return Instant.now().isAfter(createdAt.plusSeconds(expiresIn));
    }

    /**
     * Create a token with an expiration and a refresh_token.
     *
     * @param accessToken  Access token.
     * @param refreshToken Refresh token.
     * @param expiresIn    Remaining token lifetime in seconds.
     *
     * @return A new Token.
     */
    public static Token expireAfter(String accessToken, String refreshToken, long expiresIn) {
      return new Token(accessToken, refreshToken, Instant.now(), expiresIn);
    }

    /**
     * Create a token that does not have a refresh_token.
     *
     * @param accessToken Access token.
     * @param expiresIn   Remaining token lifetime in seconds.
     *
     * @return A new Token.
     */
    public static Token expireAfter(String accessToken, long expiresIn) {
      return expireAfter(accessToken, null, expiresIn);
    }

    /**
     * Create a token that never expires.
     *
     * @param accessToken Access token.
     * @return A new Token.
     */
    public static Token expireNever(String accessToken) {
      return Token.expireAfter(accessToken, -1);
    }
  }

  public static TokenProvider staticToken(String accessToken) {
    final var token = Token.expireNever(accessToken);
    return () -> token;
  }

  /**
   * Create a TokenProvider that uses an existing access_token + refresh_token
   * pair.
   *
   * @param oidc         OIDC config.
   * @param accessToken  Access token.
   * @param refreshToken Refresh token.
   * @param expiresIn    Remaining token lifetime in seconds.
   *
   * @return Internal TokenProvider implementation.
   * @throws WeaviateOAuthException if an error occurred at any point of the
   *                                exchange process.
   */
  public static TokenProvider bearerToken(OidcConfig oidc, String accessToken, String refreshToken, long expiresIn) {
    final var token = Token.expireAfter(accessToken, refreshToken, expiresIn);
    final var provider = NimbusTokenProvider.refreshToken(oidc, token);
    return reuse(token, provider);
  }

  /**
   * Create a TokenProvider that uses Resource Owner Password authorization grant.
   *
   * @param oidc     OIDC config.
   * @param username Resource owner username.
   * @param password Resource owner password.
   *
   * @return Internal TokenProvider implementation.
   * @throws WeaviateOAuthException if an error occured at any point of the token
   *                                exchange process.
   */
  public static TokenProvider resourceOwnerPassword(OidcConfig oidc, String username, String password) {
    final var passwordGrant = NimbusTokenProvider.resourceOwnerPassword(oidc, username, password);
    return reuse(null, exchange(oidc, passwordGrant));
  }

  /**
   * Create a TokenProvider that uses Client Credentials authorization grant.
   *
   * @param oidc         OIDC config.
   * @param clientId     Client ID.
   * @param clientSecret Client secret.
   *
   * @return Internal TokenProvider implementation.
   * @throws WeaviateOAuthException if an error occured at any point while
   *                                obtaining a new token.
   */
  public static TokenProvider clientCredentials(OidcConfig oidc, String clientId, String clientSecret) {
    final var provider = NimbusTokenProvider.clientCredentials(oidc, clientId, clientSecret);
    return reuse(null, provider);
  }

  /**
   * Obtain a TokenProvider that exchanges an authorization grant for a new Token.
   */
  static TokenProvider exchange(OidcConfig oidc, TokenProvider tp) {
    return new ExchangeTokenProvider(oidc, tp);
  }

  /**
   * Obtain a TokenProvider which reuses tokens obtained
   * from another TokenProvider until they expire.
   */
  static TokenProvider reuse(Token t, TokenProvider tp) {
    return ReuseTokenProvider.wrap(t, tp);
  }

  public record ProviderMetadata(URI tokenEndpoint) {
  }

  /**
   * Returns true if this OIDC provider's token endpoint is hosted at
   * {@code login.microsoftonline.com}.
   *
   * @param oidc OIDC config.
   *
   * @throws WeaviateOAuthException if metadata could not be parsed.
   */
  public static boolean isMicrosoft(OidcConfig oidc) {
    var metadata = NimbusTokenProvider.parseProviderMetadata(oidc.providerMetadata());
    return metadata.tokenEndpoint().getHost().contains("login.microsoftonline.com");
  }

}
