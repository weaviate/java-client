package technology.semi.weaviate.client.v1.auth.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class AccessTokenProviderTest extends TestCase {

  @Test
  public void testRefreshingOfToken() throws InterruptedException {
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

      @Override
      public void shutdown() {
        executor.shutdown();
      }

      private void scheduleRefreshTokenTask(long period) {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> accessToken = "NEW_VALUE", period, period, TimeUnit.SECONDS);
      }
    }
    // given
    AccessTokenProvider tokenProvider = new AuthProviderImpl("OLD_VALUE", 2);
    // when then
    Assert.assertEquals("OLD_VALUE", tokenProvider.getAccessToken());
    Thread.sleep(3000);
    // will properly shutdown
    tokenProvider.shutdown();
    Assert.assertEquals("NEW_VALUE", tokenProvider.getAccessToken());
  }
}
