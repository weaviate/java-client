package io.weaviate.client6.v1.internal;

import java.time.Instant;

import io.weaviate.client6.v1.internal.oidc.OidcConfig;
import io.weaviate.client6.v1.internal.oidc.nimbus.NimbusTokenProvider;

@FunctionalInterface
public interface TokenProvider {
  Token getToken();

  public record Token(String accessToken, String refreshToken, Instant createdAt, long expiresIn) {
    public boolean isValid() {
      if (expiresIn == -1) {
        return true;
      }
      // TODO: adjust for expireDelta
      return Instant.now().isAfter(createdAt.plusSeconds(expiresIn));
    }

    public static Token expireAfter(String accessToken, String refreshToken, long expiresIn) {
      return new Token(accessToken, refreshToken, Instant.now(), expiresIn);
    }

    /** Create a token that never expires. */
    public static Token expireNever(String accessToken) {
      return Token.expireAfter(accessToken, "", -1);
    }
  }

  public static TokenProvider staticToken(String accessToken) {
    final var token = Token.expireNever(accessToken);
    return () -> token;
  }

  public static TokenProvider reuse(Token t, TokenProvider tp) {
    return ReuseTokenProvider.wrap(t, tp);
  }

  public static TokenProvider bearerToken(OidcConfig oidc, String accessToken, String refreshToken, long expiresIn) {
    final var token = Token.expireAfter(accessToken, refreshToken, expiresIn);
    final var provider = NimbusTokenProvider.bearerToken(oidc, token);
    return reuse(token, provider);
  }

  public static TokenProvider resourceOwnerPassword(OidcConfig oidc, String username, String password) {
    final var provider = NimbusTokenProvider.resourceOwnerPassword(oidc, username, password);
    return reuse(null, provider);
  }
}
