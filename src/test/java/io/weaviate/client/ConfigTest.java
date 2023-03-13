package io.weaviate.client;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

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
}
