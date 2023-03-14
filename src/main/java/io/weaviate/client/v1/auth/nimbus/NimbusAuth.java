package io.weaviate.client.v1.auth.nimbus;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
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
import io.weaviate.client.v1.auth.provider.AuthClientCredentialsTokenProvider;
import io.weaviate.client.v1.auth.provider.AuthRefreshTokenProvider;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.weaviate.client.Config;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class NimbusAuth extends BaseAuth {

  public NimbusAuth() {
    super();
  }

  public AccessTokenProvider getAccessTokenProvider(Config config,
    String clientSecret, String username, String password, List<String> clientScopes,
    AuthType authType) throws AuthException {
    BaseAuth.AuthResponse authResponse = getIdAndTokenEndpoint(config);
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

    return getTokenProvider(config, authResponse, clientScopes,
      accessToken.getValue(), accessToken.getLifetime(), refreshTokenValue, clientSecret, authType);
  }

  protected AccessTokenProvider getTokenProvider(Config config, BaseAuth.AuthResponse authResponse, List<String> clientScopes,
    String accessToken, long accessTokenLifeTime, String refreshToken, String clientSecret, AuthType authType) {
    if (authType == AuthType.CLIENT_CREDENTIALS) {
      return new AuthClientCredentialsTokenProvider(config, authResponse, clientScopes, accessToken, accessTokenLifeTime, clientSecret);
    }
    return new AuthRefreshTokenProvider(config, authResponse, accessToken, accessTokenLifeTime, refreshToken);
  }

  public String refreshToken(Config config, BaseAuth.AuthResponse authResponse, String refreshToken) {
    try {
      OIDCTokenResponse oidcTokenResponse = getOIDCTokenResponse(config, authResponse,
        "", "", "", refreshToken, null, AuthType.REFRESH_TOKEN);
      return oidcTokenResponse.getOIDCTokens().getAccessToken().getValue();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String refreshClientCredentialsToken(Config config, BaseAuth.AuthResponse authResponse, List<String> clientScopes, String clientSecret) {
    try {
      OIDCTokenResponse oidcTokenResponse = getOIDCTokenResponse(config, authResponse,
        clientSecret, "", "", "", clientScopes, AuthType.CLIENT_CREDENTIALS);
      return oidcTokenResponse.getOIDCTokens().getAccessToken().getValue();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void logNoRefreshTokenWarning(long accessTokenLifetime) {
    String msgFormat = "Auth002: Your access token is valid for %s and no refresh token was provided.";
    log(String.format(msgFormat, getAccessTokenExpireDate(accessTokenLifetime)));
  }

  private OIDCTokenResponse getOIDCTokenResponse(Config config, BaseAuth.AuthResponse authResponse,
    String clientSecret, String username, String password, String refreshToken, List<String> clientScopes,
    AuthType authType) throws AuthException {
    try {
      OIDCProviderMetadata providerMetadata = OIDCProviderMetadata.parse(authResponse.getConfiguration());
      ClientID clientID = new ClientID(authResponse.getClientId());
      Secret secret = new Secret(clientSecret);
      String redirectURL = String.format("%s%s", config.getBaseURL(), BaseAuth.OIDC_URL);
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

  private Scope getScopes(BaseAuth.AuthResponse authResponse, List<String> clientScopes, ClientID clientID, OIDCProviderMetadata providerMetadata) {
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



  private String getAccessTokenExpireDate(Long accessTokenLifetime) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.SECOND, accessTokenLifetime.intValue());
    return dateFormat.format(cal.getTime());
  }

  private void log(String msg) {
    System.out.println(msg);
  }
}
