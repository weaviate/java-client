package io.weaviate.client.v1.auth;

import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.auth.nimbus.BaseAuth;
import io.weaviate.client.v1.auth.nimbus.NimbusAuth;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.auth.provider.AuthRefreshTokenProvider;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;

public class BearerTokenFlow implements Authentication {

  private final NimbusAuth nimbusAuth;
  private final String accessToken;
  private final long accessTokenLifetime;
  private final String refreshToken;

  public BearerTokenFlow(String accessToken, long accessTokenLifetime, String refreshToken) {
    this.nimbusAuth = new NimbusAuth();
    this.accessToken = accessToken;
    this.accessTokenLifetime = accessTokenLifetime;
    this.refreshToken = refreshToken;
  }

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    if (StringUtils.isBlank(refreshToken)) {
      nimbusAuth.logNoRefreshTokenWarning(accessTokenLifetime);
    }
    BaseAuth.AuthResponse authResponse = nimbusAuth.getIdAndTokenEndpoint(config);
    AccessTokenProvider tokenProvider = new AuthRefreshTokenProvider(config,
      authResponse, accessToken, accessTokenLifetime, refreshToken);
    return new WeaviateClient(config, tokenProvider);
  }

  @Override
  public WeaviateClient getAuthClient(Config config) throws AuthException {
    return getAuthClient(config, null);
  }
}
