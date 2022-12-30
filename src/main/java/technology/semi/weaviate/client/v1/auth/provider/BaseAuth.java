package technology.semi.weaviate.client.v1.auth.provider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.impl.client.HttpClientBuilder;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.Serializer;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.http.HttpResponse;
import technology.semi.weaviate.client.base.http.impl.CommonsHttpClientImpl;

class BaseAuth {
  protected final String OIDC_URL = "/.well-known/openid-configuration";
  private final Serializer serializer;

  BaseAuth() {
    this.serializer = new Serializer();
  }

  @Getter
  @AllArgsConstructor
  public class AuthResponse {
    String clientId;
    String[] scopes;
    String configuration;
  }

  @Getter
  private class OIDCConfig {
    String clientId;
    String href;
    String[] scopes;
  }

  protected AuthResponse getIdAndTokenEndpoint(Config config) throws AuthException {
    HttpClientBuilder builder = HttpClientBuilder.create();
    HttpClient client = new CommonsHttpClientImpl(config.getHeaders(), builder::build);
    String url = config.getBaseURL() + OIDC_URL;
    HttpResponse response = sendGetRequest(client, url);
    switch (response.getStatusCode()) {
      case 404:
        String msg = "Auth001: The client was configured to use authentication, but weaviate is configured without authentication. Are you sure this is " +
          "correct?";
        log(msg);
        return null;
      case 200:
        OIDCConfig oidcConfig = serializer.toResponse(response.getBody(), OIDCConfig.class);
        HttpResponse resp = sendGetRequest(client, oidcConfig.getHref());
        if (resp.getStatusCode() != 200) {
          String errorMessage = String.format("OIDC configuration url %s returned status code %s", oidcConfig.getHref(), response.getStatusCode());
          throw new AuthException(errorMessage);
        }
        return new AuthResponse(oidcConfig.getClientId(), oidcConfig.getScopes(), resp.getBody());
      default:
        String errorMessage = String.format("OIDC configuration url %s returned status code %s", url, response.getStatusCode());
        throw new AuthException(errorMessage);
    }
  }

  private HttpResponse sendGetRequest(HttpClient client, String url) throws AuthException {
    try {
      return client.sendGetRequest(url);
    } catch (Exception e) {
      throw new AuthException(e);
    }
  }

  protected void logNoRefreshTokenWarning(long accessTokenLifetime) {
    String msgFormat = "Auth002: Your access token is valid for %s and no refresh token was provided.";
    log(String.format(msgFormat, getAccessTokenExpireDate(accessTokenLifetime)));
  }

  private String getAccessTokenExpireDate(Long accessTokenLifetime) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.SECOND, accessTokenLifetime.intValue());
    return dateFormat.format(cal.getTime());
  }

  protected void log(String msg) {
    System.out.println(msg);
  }
}
