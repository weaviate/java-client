package io.weaviate.client6.v1.api;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ClientVersionTest {

  private static final String HEADER_KEY = "X-Weaviate-Client";

  @Test
  public void testHeaderPresence() {
    Config config = new Config.Local().build();
    Assertions.assertThat(config.headers()).containsKey(HEADER_KEY);
    Assertions.assertThat(config.headers().get(HEADER_KEY)).isNotEmpty();

    config = new Config.WeaviateCloud("http://localhost/", Authentication.apiKey("test_key")).build();
    Assertions.assertThat(config.headers()).containsKey(HEADER_KEY);
    Assertions.assertThat(config.headers().get(HEADER_KEY)).isNotEmpty();

    config = new Config.Custom().httpHost("localhost").build();
    Assertions.assertThat(config.headers()).containsKey(HEADER_KEY);
    Assertions.assertThat(config.headers().get(HEADER_KEY)).isNotEmpty();
  }
}
