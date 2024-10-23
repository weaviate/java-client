package io.weaviate.client.v1.misc;

import io.weaviate.client.Config;
import io.weaviate.client.v1.misc.async.MetaGetter;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class AsyncMisc {
  private final CloseableHttpAsyncClient client;
  private final Config config;

  public AsyncMisc(CloseableHttpAsyncClient client, Config config) {
    this.client = client;
    this.config = config;
  }

  public MetaGetter metaGetter() {
    return new MetaGetter(client, config);
  }
}
