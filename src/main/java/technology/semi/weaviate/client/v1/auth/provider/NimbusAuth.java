package technology.semi.weaviate.client.v1.auth.provider;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;

public class NimbusAuth extends BaseAuth {

  public NimbusAuth() {
    super();
  }

  protected WeaviateClient getAuthClient(Config config,
    String clientSecret, String username, String password, List<String> clientScopes,
    AuthType authType) throws AuthException {
    AuthResponse authResponse = getIdAndTokenEndpoint(config);
    OIDCTokenResponse oidcTokenResponse = getOIDCTokenResponse(config, authResponse,
      clientSecret, username, password, "", clientScopes, authType);
    AccessToken accessToken = oidcTokenResponse.getOIDCTokens().getAccessToken();
    RefreshToken refreshToken = oidcTokenResponse.getOIDCTokens().getRefreshToken();

    String refreshTokenValue = null;
    if (refreshToken != null) {
      refreshTokenValue = refreshToken.getValue();
    } else {
      logNoRefreshTokenWarning(accessToken.getLifetime());
    }

    return getWeaviateClient(config, authResponse,
      accessToken.getValue(), accessToken.getLifetime(), refreshTokenValue);
  }

  protected WeaviateClient getWeaviateClient(Config config, AuthResponse authResponse,
    String accessToken, long accessTokenLifeTime, String refreshToken) {
    Config authConfig = AuthConfigUtil.toAuthConfig(config, authResponse,
      accessToken, accessTokenLifeTime, refreshToken);
    return new WeaviateClient(authConfig);
  }

  protected String refreshToken(Config config, AuthResponse authResponse, String refreshToken) {
    try {
      OIDCTokenResponse oidcTokenResponse = getOIDCTokenResponse(config, authResponse,
        "", "", "", refreshToken, null, AuthType.REFRESH_TOKEN);
      return oidcTokenResponse.getOIDCTokens().getAccessToken().getValue();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected OIDCTokenResponse getOIDCTokenResponse(Config config, AuthResponse authResponse,
    String clientSecret, String username, String password, String refreshToken, List<String> clientScopes,
    AuthType authType) throws AuthException {
    try {
      OIDCProviderMetadata providerMetadata = OIDCProviderMetadata.parse(authResponse.getConfiguration());
      ClientID clientID = new ClientID(authResponse.getClientId());
      Secret secret = new Secret(clientSecret);
      String redirectURL = String.format("%s%s", config.getBaseURL(), OIDC_URL);
      String responseTypes = "code id_token";
      String responseMode = "fragment";
      Scope scopes = getScopes(authResponse, clientScopes, clientID, providerMetadata);
      Map<String, List<String>> customParams = new HashMap<>();
      customParams.put("response_type", Collections.singletonList(responseTypes));
      customParams.put("response_mode", Collections.singletonList(responseMode));
      customParams.put("redirect_url", Collections.singletonList(redirectURL));

      TokenRequest tokenReq = new TokenRequest(providerMetadata.getTokenEndpointURI(),
        new ClientSecretPost(clientID, secret),
        getAuthorizationGrant(authType, username, password, refreshToken),
        scopes, null, customParams);

      HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
      TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);

      if (tokenResponse instanceof TokenErrorResponse) {
        ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
        throw new RuntimeException(error.getDescription());
      }

      OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
      return oidcTokenResponse;
    } catch (Throwable e) {
      throw new AuthException(e.getMessage(), e);
    }
  }

  private Scope getScopes(AuthResponse authResponse, List<String> clientScopes, ClientID clientID, OIDCProviderMetadata providerMetadata) {
    Scope scopes = new Scope();
    if (authResponse.getScopes() != null) {
      Arrays.stream(authResponse.getScopes()).forEach(scopes::add);
    }
    if (clientScopes != null) {
      clientScopes.forEach(scopes::add);
    }
    if (scopes.isEmpty()) {
      if (providerMetadata.getTokenEndpointURI().getHost().equals("login.microsoftonline.com")) {
        scopes.add(clientID + "/.default");
      }
    }
    return scopes;
  }

  private AuthorizationGrant getAuthorizationGrant(AuthType authType, String username, String password, String refreshToken) {
    switch (authType) {
      case USER_PASSWORD:
        return new ResourceOwnerPasswordCredentialsGrant(username, new Secret(password));
      case CLIENT_CREDENTIALS:
        return new ClientCredentialsGrant();
      default:
        return new RefreshTokenGrant(new RefreshToken(refreshToken));
    }
  }
}
