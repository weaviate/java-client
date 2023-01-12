package technology.semi.weaviate.client.v1.auth.provider;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;

public class AuthClientCredentialsTokenProvider extends NimbusAuth implements AccessTokenProvider {
  private String accessToken;
  private ScheduledExecutorService executor;

  public AuthClientCredentialsTokenProvider(Config config, AuthResponse authResponse, List<String> clientScopes,
    String accessToken, long lifetimeSeconds, String clientSecret) {
    this.accessToken = accessToken;
    scheduleRefreshTokenTask(config, authResponse, clientScopes, clientSecret, lifetimeSeconds);
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  private void scheduleRefreshTokenTask(Config config, AuthResponse authResponse, List<String> clientScopes, String clientSecret, long period) {
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(() -> {
      accessToken = refreshClientCredentialsToken(config, authResponse, clientScopes, clientSecret);
    }, period, period, TimeUnit.SECONDS);
  }
}
