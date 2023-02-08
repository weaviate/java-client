package technology.semi.weaviate.client.v1.auth.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.auth.nimbus.BaseAuth;
import technology.semi.weaviate.client.v1.auth.nimbus.NimbusAuth;

public class AuthRefreshTokenProvider implements AccessTokenProvider {
  private final NimbusAuth nimbusAuth;
  private String accessToken;
  private ScheduledExecutorService executor;

  public AuthRefreshTokenProvider(Config config, BaseAuth.AuthResponse authResponse, String accessToken, long lifetimeSeconds, String refreshToken) {
    this.nimbusAuth = new NimbusAuth();
    this.accessToken = accessToken;
    if (StringUtils.isNotBlank(refreshToken)) {
      scheduleRefreshTokenTask(config, authResponse, refreshToken, lifetimeSeconds);
    }
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }

  private void scheduleRefreshTokenTask(Config config, BaseAuth.AuthResponse authResponse, String refreshToken, long period) {
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(() -> accessToken = nimbusAuth.refreshToken(config, authResponse, refreshToken),
      period, period, TimeUnit.SECONDS);
  }
}
