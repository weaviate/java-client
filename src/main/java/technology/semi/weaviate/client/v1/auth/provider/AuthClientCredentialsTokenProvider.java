package technology.semi.weaviate.client.v1.auth.provider;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.auth.nimbus.BaseAuth;
import technology.semi.weaviate.client.v1.auth.nimbus.NimbusAuth;

public class AuthClientCredentialsTokenProvider implements AccessTokenProvider {

  private final NimbusAuth nimbusAuth;
  private String accessToken;
  private ScheduledExecutorService executor;

  public AuthClientCredentialsTokenProvider(Config config, BaseAuth.AuthResponse authResponse, List<String> clientScopes,
    String accessToken, long lifetimeSeconds, String clientSecret) {
    this.nimbusAuth = new NimbusAuth();
    this.accessToken = accessToken;
    scheduleRefreshTokenTask(config, authResponse, clientScopes, clientSecret, lifetimeSeconds);
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  public void shutdown() {
    executor.shutdown();
  }

  private void scheduleRefreshTokenTask(Config config, BaseAuth.AuthResponse authResponse, List<String> clientScopes, String clientSecret, long period) {
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(() -> accessToken = nimbusAuth.refreshClientCredentialsToken(config, authResponse, clientScopes, clientSecret),
      period, period, TimeUnit.SECONDS);
  }
}
