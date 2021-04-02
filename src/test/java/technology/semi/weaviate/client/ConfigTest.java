package technology.semi.weaviate.client;

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
}