package io.weaviate.client6.v1.internal;

import io.weaviate.client6.v1.internal.oidc.OidcConfig;

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
}
