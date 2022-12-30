package technology.semi.weaviate.client.v1.auth.provider;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;
import technology.semi.weaviate.client.Config;

class AuthConfigUtilTest {

  @Test
  void testGetAuthConfig() {
    // given
    String scheme = "https";
    String host = "sandbox.network.com";
    String token = "token";
    // when
    Config config = new Config(scheme, host);
    Config authConfig = AuthConfigUtil.toAuthConfig(config, null, token, 0l, null);
    // then
    assertEquals("https://sandbox.network.com/v1", authConfig.getBaseURL());
    assertEquals(String.format("Bearer %s", token), authConfig.getHeaders().get("Authorization"));
  }

  @Test
  void testGetAuthConfigWithHeaders() {
    // given
    String scheme = "http";
    String host = "sandbox.network.com";
    String token = "token";
    Map<String, String> headers = new HashMap<>();
    headers.put("X-Some-Key", "some value");
    // when
    Config config = new Config(scheme, host, headers);
    Config authConfig = AuthConfigUtil.toAuthConfig(config, null, token, 0l, null);
    // then
    assertEquals("http://sandbox.network.com/v1", authConfig.getBaseURL());
    assertEquals(String.format("Bearer %s", token), authConfig.getHeaders().get("Authorization"));
    assertEquals("some value", authConfig.getHeaders().get("X-Some-Key"));
  }
}
