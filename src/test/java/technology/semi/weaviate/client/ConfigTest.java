package technology.semi.weaviate.client;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import technology.semi.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class ConfigTest extends TestCase {
  @Test
  public void testConfig() {
    // given
    String scheme = "https";
    String domain = "localhost:8080";
    // when
    Config config = new Config(scheme, domain);
    // then
    Assert.assertEquals("https://localhost:8080/v1", config.getBaseURL());
  }

  @Test
  public void testConfigHeaders() {
    // given
    String scheme = "https";
    String domain = "localhost:8080";
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer valueA");
    headers.put("Authorization", "Bearer valueB");
    // when
    Config config = new Config(scheme, domain, headers);
    // then
    Assert.assertEquals("https://localhost:8080/v1", config.getBaseURL());
    Assert.assertEquals(1, config.getHeaders().size());
  }

  @Test
  public void testConfigAuthProvider() throws InterruptedException {
    // given

    class AuthProviderImpl implements AccessTokenProvider {
      private String accessToken;
      private ScheduledExecutorService executor;

      public AuthProviderImpl(String accessToken, long period) {
        this.accessToken = accessToken;
        scheduleRefreshTokenTask(period);
      }
      @Override
      public String getAccessToken() {
        return accessToken;
      }

      private void scheduleRefreshTokenTask(long period) {
        TimerTask refreshTokenTask = new TimerTask() {
          public void run() {
            accessToken = "NEW_VALUE";
          }
        };
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(refreshTokenTask, period, period, TimeUnit.SECONDS);
      }
    }

    String scheme = "https";
    String domain = "localhost:8080";
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer valueA");
    headers.put("Authorization", "Bearer valueB");
    AccessTokenProvider provider = new AuthProviderImpl("OLD_VALUE", 2);
    // when
    Config config = new Config(scheme, domain, headers, provider);
    // then
    Assert.assertEquals("https://localhost:8080/v1", config.getBaseURL());
    Assert.assertEquals(1, config.getHeaders().size());
    Assert.assertEquals("Bearer OLD_VALUE", config.getHeaders().get("Authorization"));
    Thread.sleep(3000);
    Assert.assertEquals("Bearer NEW_VALUE", config.getHeaders().get("Authorization"));
  }
}
