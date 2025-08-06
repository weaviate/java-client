package io.weaviate.client6.v1.internal.oidc.nimbus;

import javax.annotation.concurrent.NotThreadSafe;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import io.weaviate.client6.v1.internal.TokenProvider.Token;

@NotThreadSafe
final class BearerTokenFlow implements Flow {
  private Token t;

  BearerTokenFlow(Token t) {
    this.t = t;
  }

  @Override
  public AuthorizationGrant getAuthorizationGrant() {
    return new RefreshTokenGrant(new RefreshToken(t.refreshToken()));
  }

  public void setToken(Token t) {
    this.t = t;
  }
}
