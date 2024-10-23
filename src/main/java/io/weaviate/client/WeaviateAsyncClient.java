package io.weaviate.client;

import io.weaviate.client.base.http.async.AsyncHttpClient;
import io.weaviate.client.v1.misc.AsyncMisc;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.io.CloseMode;

public class WeaviateAsyncClient implements AutoCloseable {
  private final Config config;
  private final CloseableHttpAsyncClient client;

  WeaviateAsyncClient(Config config) {
    this.config = config;
    this.client = AsyncHttpClient.create(config);
    // auto start the client
    this.start();
  }

  public AsyncMisc misc() {
    return new AsyncMisc(client, config);
  }

  private void start() {
    this.client.start();
  }

  @Override
  public void close() {
    this.client.close(CloseMode.GRACEFUL);
  }
}
