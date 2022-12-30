package technology.semi.weaviate.client.v1.auth.provider;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;

public class AuthTokenProvider extends NimbusAuth implements AccessTokenProvider {
  private String accessToken;
  private ScheduledExecutorService executor;

  public AuthTokenProvider(Config config, AuthResponse authResponse, String accessToken, long lifetimeSeconds, String refreshToken) {
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
    TimerTask refreshTokenTask = new TimerTask() {
      public void run() {
        accessToken = refreshToken(config, authResponse, refreshToken);
      }
    };
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(refreshTokenTask, period, period, TimeUnit.SECONDS);
  }

//  public void stopRefreshTokenTask() {
//    executor.shutdown();
//  }
}
