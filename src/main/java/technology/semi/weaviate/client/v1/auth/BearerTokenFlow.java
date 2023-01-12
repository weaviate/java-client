package technology.semi.weaviate.client.v1.auth;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.auth.provider.AuthConfigUtil;
import technology.semi.weaviate.client.v1.auth.provider.AuthException;
import technology.semi.weaviate.client.v1.auth.provider.NimbusAuth;

public class BearerTokenFlow extends NimbusAuth implements Authentication {
  private final String accessToken;
  private final long accessTokenLifetime;
  private final String refreshToken;

  public BearerTokenFlow(String accessToken, long accessTokenLifetime, String refreshToken) {
    super();
    this.accessToken = accessToken;
    this.accessTokenLifetime = accessTokenLifetime;
    this.refreshToken = refreshToken;
  }

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    if (StringUtils.isBlank(refreshToken)) {
      logNoRefreshTokenWarning(accessTokenLifetime);
    }
    AuthResponse authResponse = getIdAndTokenEndpoint(config);
    Config authConfig = AuthConfigUtil.refreshTokenConfig(config, authResponse,
      accessToken, accessTokenLifetime, refreshToken);
    return new WeaviateClient(authConfig);
  }

  @Override
  public WeaviateClient getAuthClient(Config config) throws AuthException {
    return getAuthClient(config, null);
  }
}
