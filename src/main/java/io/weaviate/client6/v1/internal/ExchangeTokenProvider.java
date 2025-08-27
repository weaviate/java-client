package io.weaviate.client6.v1.internal;

import io.weaviate.client6.v1.internal.oidc.OidcConfig;

/**
 * ExchangeTokenProvider obtains a new {@link Token} from "single-use"
 * {@link TokenProvider}, usually one using an Resource Owner Password grant.
 * It then creates a new internal TokenProvider to refresh the token each time
 * {@link #getToken} is called.
 *
 * <p>
 * Usage:
 *
 * <pre>{@code
 * var initialGrant = TokenProvider.resourceOwnerPassword(oidc, username, password);
 * var exchange = new ExchangeTokenProvider(oidc, initialGrant);
 * var token = exchange.getToken();
 * } </pre>
 */
class ExchangeTokenProvider implements TokenProvider {
  private final TokenProvider bearer;

  ExchangeTokenProvider(OidcConfig oidc, TokenProvider tp) {
    var t = tp.getToken();
    this.bearer = TokenProvider.bearerToken(oidc, t.accessToken(), t.refreshToken(), t.expiresIn());
  }

  @Override
  public Token getToken() {
    return bearer.getToken();
  }

  @Override
  public void close() throws Exception {
    bearer.close();
  }
}
