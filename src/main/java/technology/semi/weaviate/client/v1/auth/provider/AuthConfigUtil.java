package technology.semi.weaviate.client.v1.auth.provider;

import technology.semi.weaviate.client.Config;

public class AuthConfigUtil {

  public static Config toAuthConfig(Config config, BaseAuth.AuthResponse authResponse, String accessToken, long accessTokenLifetime, String refreshToken) {
    String baseURL = config.getBaseURL();
    String scheme = getScheme(baseURL);
    String host = getHost(baseURL);
    AuthTokenProvider authTokenProvider = new AuthTokenProvider(config, authResponse, accessToken, accessTokenLifetime, refreshToken);
    return new Config(scheme, host, config.getHeaders(), authTokenProvider);
  }

  private static String getScheme(String baseURL) {
    return baseURL.substring(0, baseURL.indexOf("://"));
  }

  private static String getHost(String baseURL) {
    String base = baseURL.substring(baseURL.indexOf("://") + 3);
    return base.substring(0, base.indexOf("/"));
  }
}
