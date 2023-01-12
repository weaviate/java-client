package technology.semi.weaviate.client.v1.auth.provider;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Test;
import technology.semi.weaviate.client.Config;

public class AuthConfigUtilTest extends TestCase {

  @Test
  public void testGetAuthConfig() {
    // given
    String scheme = "https";
    String host = "sandbox.network.com";
    String token = "token";
    // when
    Config config = new Config(scheme, host);
    Config authConfig = AuthConfigUtil.refreshTokenConfig(config, null, token, 0l, null);
    // then
    assertEquals("https://sandbox.network.com/v1", authConfig.getBaseURL());
    assertEquals(String.format("Bearer %s", token), authConfig.getHeaders().get("Authorization"));
  }

  @Test
  public void testGetAuthConfigWithHeaders() {
    // given
    String scheme = "http";
    String host = "sandbox.network.com";
    String token = "token";
    Map<String, String> headers = new HashMap<>();
    headers.put("X-Some-Key", "some value");
    // when
    Config config = new Config(scheme, host, headers);
    Config authConfig = AuthConfigUtil.refreshTokenConfig(config, null, token, 0l, null);
    // then
    assertEquals("http://sandbox.network.com/v1", authConfig.getBaseURL());
    assertEquals(String.format("Bearer %s", token), authConfig.getHeaders().get("Authorization"));
    assertEquals("some value", authConfig.getHeaders().get("X-Some-Key"));
  }
}
