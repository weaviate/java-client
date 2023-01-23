package technology.semi.weaviate.client.v1.auth.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;

public class AuthRefreshTokenProvider extends NimbusAuth implements AccessTokenProvider {
  private String accessToken;
  private ScheduledExecutorService executor;

  public AuthRefreshTokenProvider(Config config, AuthResponse authResponse, String accessToken, long lifetimeSeconds, String refreshToken) {
    this.accessToken = accessToken;
    if (StringUtils.isNotBlank(refreshToken)) {
      scheduleRefreshTokenTask(config, authResponse, refreshToken, lifetimeSeconds);
    }
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  private void scheduleRefreshTokenTask(Config config, AuthResponse authResponse, String refreshToken, long period) {
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(() -> {
      accessToken = refreshToken(config, authResponse, refreshToken);
    }, period, period, TimeUnit.SECONDS);
  }
}