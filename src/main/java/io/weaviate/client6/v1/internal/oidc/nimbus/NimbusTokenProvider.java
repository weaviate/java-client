package io.weaviate.client6.v1.internal.oidc.nimbus;

import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import io.weaviate.client6.v1.api.WeaviateOAuthException;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.oidc.OidcConfig;

@NotThreadSafe
public final class NimbusTokenProvider implements TokenProvider {
  private final OIDCProviderMetadata metadata;
  private final ClientID clientId;
  private final Scope scope;
  private final Flow flow;

  /**
   * Create a TokenProvider that uses Refresh Token authorization grant.
   *
   * @param oidc OIDC config.
   * @param t    Current token. Must not be null.
   *
   * @return A new instance of NimbusTokenProvider. Instances are never cached.
   * @throws WeaviateOAuthException if an error occurred at any point of the
   *                                exchange process.
   */
  public static NimbusTokenProvider refreshToken(OidcConfig oidc, Token t) {
    return new NimbusTokenProvider(oidc, Flow.refreshToken(t));
  }

  /**
   * Create a TokenProvider that uses Resource Owner Password authorization grant.
   *
   * @param oidc     OIDC config.
   * @param username Resource owner username.
   * @param password Resource owner password.
   *
   * @return A new instance of NimbusTokenProvider. Instances are never cached.
   * @throws WeaviateOAuthException if an error occured at any point of the
   *                                exchange process.
   */
  public static NimbusTokenProvider resourceOwnerPassword(OidcConfig oidc, String username, String password) {
    return new NimbusTokenProvider(oidc, Flow.resourceOwnerPassword(username, password));
  }

  /**
   * Create a TokenProvider that uses Client Credentials authorization grant.
   *
   * @param oidc         OIDC config.
   * @param clientId     Client ID.
   * @param clientSecret Client secret.
   *
   * @return A new instance of NimbusTokenProvider. Instances are never cached.
   * @throws WeaviateOAuthException if an error occured at any point of the
   *                                exchange process.
   */
  public static NimbusTokenProvider clientCredentials(OidcConfig oidc, String clientId, String clientSecret) {
    return new NimbusTokenProvider(oidc, Flow.clientCredentials(clientId, clientSecret));
  }

  private NimbusTokenProvider(OidcConfig oidc, Flow flow) {
    this.metadata = _parseProviderMetadata(oidc.providerMetadata());
    this.clientId = new ClientID(oidc.clientId());
    this.scope = new Scope(oidc.scopes().toArray(String[]::new));
    this.flow = flow;
  }

  @Override
  public Token getToken() {
    var uri = metadata.getTokenEndpointURI();
    var grant = flow.getAuthorizationGrant();

    var clientAuth = flow.getClientAuthentication();
    var tokenRequest = clientAuth == null
        ? new TokenRequest(uri, clientId, grant, scope)
        : new TokenRequest(uri, clientAuth, grant, scope);
    var request = tokenRequest.toHTTPRequest();

    OIDCTokens tokens;
    try {
      var response = request.send();
      tokens = OIDCTokensParser.parse(response);
    } catch (IOException | ParseException e) {
      throw new WeaviateOAuthException(e);
    }

    var accessToken = tokens.getAccessToken();
    var refreshToken = tokens.getRefreshToken();

    var newToken = refreshToken == null
        ? Token.expireAfter(accessToken.getValue(), accessToken.getLifetime())
        : Token.expireAfter(accessToken.getValue(), refreshToken.getValue(), accessToken.getLifetime());

    if (flow instanceof RefreshTokenFlow rtf) {
      rtf.setToken(newToken);
    }

    return newToken;
  }

  public static ProviderMetadata parseProviderMetadata(String providerMetadata) {
    var metadata = _parseProviderMetadata(providerMetadata);
    return new ProviderMetadata(metadata.getTokenEndpointURI());
  }

  private static OIDCProviderMetadata _parseProviderMetadata(String providerMetadata) {
    try {
      return OIDCProviderMetadata.parse(providerMetadata);
    } catch (ParseException ex) {
      throw new WeaviateOAuthException("parse provider metadata: ", ex);
    }
  }
}
