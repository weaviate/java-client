package technology.semi.weaviate.client.v1.auth.provider;

import java.util.List;
import technology.semi.weaviate.client.Config;

public class AuthConfigUtil {

  public static Config refreshTokenConfig(Config config, BaseAuth.AuthResponse authResponse,
    String accessToken, long accessTokenLifetime, String refreshToken) {
    AccessTokenProvider provider = new AuthRefreshTokenProvider(config, authResponse, accessToken, accessTokenLifetime, refreshToken);
    return toAuthConfig(config, provider);
  }

  public static Config clientCredentialsAuthConfig(Config config, BaseAuth.AuthResponse authResponse, List<String> clientScopes,
    String accessToken, long accessTokenLifetime, String clientSecret) {
    AccessTokenProvider provider = new AuthClientCredentialsTokenProvider(config, authResponse, clientScopes, accessToken, accessTokenLifetime, clientSecret);
    return toAuthConfig(config, provider);
  }

  private static Config toAuthConfig(Config config, AccessTokenProvider accessTokenProvider) {
    String baseURL = config.getBaseURL();
    String scheme = getScheme(baseURL);
    String host = getHost(baseURL);
    return new Config(scheme, host, config.getHeaders(), accessTokenProvider);
  }

  private static String getScheme(String baseURL) {
    return baseURL.substring(0, baseURL.indexOf("://"));
  }

  private static String getHost(String baseURL) {
    String base = baseURL.substring(baseURL.indexOf("://") + 3);
    return base.substring(0, base.indexOf("/"));
  }
}
