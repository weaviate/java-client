package io.weaviate.client6.v1.internal.oidc.nimbus;

import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import io.weaviate.client6.v1.api.WeaviateOAuthException;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.oidc.OidcConfig;

@NotThreadSafe
public final class NimbusTokenProvider implements TokenProvider {
  private final OIDCProviderMetadata metadata;
  private final ClientID clientId;
  private final Scope scope;
  private final String redirectUrl;
  private final Flow flow;

  public static NimbusTokenProvider bearerToken(OidcConfig oidc, Token t) {
    return new NimbusTokenProvider(oidc, Flow.bearerToken(t));
  }

  public static NimbusTokenProvider resourceOwnerPassword(OidcConfig oidc, String username, String password) {
    return new NimbusTokenProvider(oidc, Flow.resourceOwnerPassword(username, password));
  }

  private NimbusTokenProvider(OidcConfig oidc, Flow flow) {
    try {
      this.metadata = OIDCProviderMetadata.parse(oidc.providerMetadata());
    } catch (ParseException ex) {
      throw new WeaviateOAuthException("parse provider metadata: ", ex);
    }

    this.clientId = new ClientID(oidc.clientId());
    this.scope = new Scope(oidc.scopes().toArray(String[]::new));
    this.redirectUrl = oidc.redirectUrl();
    this.flow = flow;
  }

  @Override
  public Token getToken() {
    var uri = metadata.getTokenEndpointURI();
    var grant = flow.getAuthorizationGrant();
    var request = new TokenRequest(uri, clientId, grant, scope).toHTTPRequest();

    TokenResponse response;
    try {
      var httpResponse = request.send();
      response = OIDCTokenResponseParser.parse(httpResponse);
    } catch (IOException | ParseException e) {
      throw new WeaviateOAuthException(e);
    }

    if (response instanceof TokenErrorResponse err) {
      var error = err.getErrorObject();
      throw new WeaviateOAuthException("%s (code=%s)".formatted(
          error.getDescription(),
          error.getCode()));
    }

    var tokens = ((OIDCTokenResponse) response).getOIDCTokens();
    var accessToken = tokens.getAccessToken();
    var refreshToken = tokens.getRefreshToken();

    var newToken = Token.expireAfter(
        accessToken.getValue(),
        refreshToken.getValue(),
        accessToken.getLifetime());

    if (flow instanceof BearerTokenFlow btf) {
      btf.setToken(newToken);
    }

    return newToken;
  }

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
}
