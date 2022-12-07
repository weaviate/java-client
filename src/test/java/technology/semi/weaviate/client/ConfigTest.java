package technology.semi.weaviate.client;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {
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
  public void testConfig_timeout_set() {
    // given
    String scheme = "https";
    String domain = "localhost:8080";
    int timeout = 5;
    // when
    Config config = new Config(scheme, domain, null, timeout);
    // then
    Assert.assertEquals(5, config.getConnectionTimeoutMs());
    Assert.assertEquals(5, config.getSocketTimeoutMs());
    Assert.assertEquals(5, config.getConnectionRequestTimeoutMs());
  }

  @Test
  public void testConfig_timeout_set_all() {
    // given
    String scheme = "https";
    String domain = "localhost:8080";
    int connectionTimeoutMs = 30;
    int connectionRequestTimeoutMs = 20;
    int socketTimeoutMs = 10;
    // when
    Config config = new Config(scheme, domain, null, connectionTimeoutMs, connectionRequestTimeoutMs, socketTimeoutMs);
    // then
    Assert.assertEquals(30, config.getConnectionTimeoutMs());
    Assert.assertEquals(20, config.getConnectionRequestTimeoutMs());
    Assert.assertEquals(10, config.getSocketTimeoutMs());
  }

}
