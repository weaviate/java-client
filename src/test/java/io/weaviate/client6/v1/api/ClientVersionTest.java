package io.weaviate.client6.v1.api;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import io.weaviate.client6.v1.internal.ClientVersion;

public class ClientVersionTest {

  @Test
  public void testHeaderPresence() {
    Config config = new Config.Local().build();
    Assertions.assertThat(config.headers()).containsKey(ClientVersion.HEADER_X_WEAVIATE_CLIENT);
    Assertions.assertThat(config.headers().get(ClientVersion.HEADER_X_WEAVIATE_CLIENT)).isNotEmpty();
  }
}
