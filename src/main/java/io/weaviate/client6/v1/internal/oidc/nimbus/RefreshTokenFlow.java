package io.weaviate.client6.v1.internal.oidc.nimbus;

import javax.annotation.concurrent.NotThreadSafe;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import io.weaviate.client6.v1.internal.TokenProvider.Token;

/**
 * RefreshTokenFlow provides {@link RefreshTokenGrant} with a refresh_token.
 * Once the caller has obtained a new {@link Token} it must be updated using
 * {@link #setToken} to ensure RefreshTokenFlow continues to return valid
 * authorization grants.
 */
@NotThreadSafe
final class RefreshTokenFlow implements Flow {
  private Token t;

  RefreshTokenFlow(Token t) {
    this.t = t;
  }

  @Override
  public AuthorizationGrant getAuthorizationGrant() {
    return new RefreshTokenGrant(new RefreshToken(t.refreshToken()));
  }

  public String getRefreshToken() {
    return t.refreshToken();
  }

  public void setToken(Token t) {
    this.t = t;
  }
}
