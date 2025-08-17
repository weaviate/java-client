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
  public record Token(String accessToken, String refreshToken, Instant createdAt, long expiresIn, long expiryDelta) {
    /**
     * Returns {@code true} if remaining lifetime of the token is greater than 0.
     * Tokens created with {@link #expireNever} are always valid.
     */
    public boolean isValid() {
      if (expiresIn == -1) {
        return true;
      }
      return Instant.now().isBefore(createdAt.plusSeconds(expiresIn - expiryDelta));
    }

    /**
     * Set early expiry for the Token.
     *
     * <p>
     * A Token with {@link #expiresIn} of 10s and {@link #expiryDelta} of 3s
     * will be invalid 7s after being created.
     *
     * @param expiryDelta Early expiry in seconds.
     * @return A Token identical to the source one, but with a different expiry.
     */
    public Token withExpiryDelta(long expiryDelta) {
      return new Token(accessToken, refreshToken, createdAt, expiresIn, expiryDelta);
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
      return new Token(accessToken, refreshToken, Instant.now(), expiresIn, 0);
    }

    /**
     * Create a token that does not have a refresh_token.
     * For example, a token obtained via a Client Credentials grant
     * can only be renewed using that grant type.
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

  /**
   * Refreshing the token slightly ahead of time will help prevent
   * phony unauthorized access errors.
   *
   * This value is currently not configurable and should be seen
   * as an internal implementation detail.
   */
  static long DEFAULT_EARLY_EXPIRY = 30;

  /**
   * Authorize using a token that never expires and doesn't need to be refreshed.
   *
   * @param accessToken Access token.
   */
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
    return reuse(token, provider, DEFAULT_EARLY_EXPIRY);
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
    return reuse(null, exchange(oidc, passwordGrant), DEFAULT_EARLY_EXPIRY);
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
    return reuse(null, provider, DEFAULT_EARLY_EXPIRY);
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

  /**
   * Obtain a TokenProvider which reuses tokens obtained
   * from another TokenProvider until they expire.
   */
  static TokenProvider reuse(Token t, TokenProvider tp, long expiryDelta) {
    return ReuseTokenProvider.wrap(t, tp, expiryDelta);
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
