package technology.semi.weaviate.client.v1.auth.nimbus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.Serializer;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.http.HttpResponse;
import technology.semi.weaviate.client.base.http.builder.HttpApacheClientBuilder;
import technology.semi.weaviate.client.base.http.impl.CommonsHttpClientImpl;
import technology.semi.weaviate.client.v1.auth.exception.AuthException;

public class BaseAuth {
  public final static String OIDC_URL = "/.well-known/openid-configuration";
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

  public AuthResponse getIdAndTokenEndpoint(Config config) throws AuthException {
    HttpClient client = new CommonsHttpClientImpl(config.getHeaders(), HttpApacheClientBuilder.build(config));
    String url = config.getBaseURL() + OIDC_URL;
    HttpResponse response = sendGetRequest(client, url);
    switch (response.getStatusCode()) {
      case 404:
        String msg = "Auth001: The client was configured to use authentication, but weaviate is configured without authentication. Are you sure this is " +
          "correct?";
        log(msg);
        throw new AuthException(msg);
      case 200:
        OIDCConfig oidcConfig = serializer.toResponse(response.getBody(), OIDCConfig.class);
        HttpResponse resp = sendGetRequest(client, oidcConfig.getHref());
        if (resp.getStatusCode() != 200) {
          String errorMessage = String.format("OIDC configuration url %s returned status code %s", oidcConfig.getHref(), resp.getStatusCode());
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

  private void log(String msg) {
    System.out.println(msg);
  }
}
