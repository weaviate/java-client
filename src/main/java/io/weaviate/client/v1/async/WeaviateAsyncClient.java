package io.weaviate.client.v1.async;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.async.AsyncHttpClient;
import io.weaviate.client.v1.async.misc.Misc;
import io.weaviate.client.v1.async.schema.Schema;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.io.CloseMode;

public class WeaviateAsyncClient implements AutoCloseable {
  private final Config config;
  private final CloseableHttpAsyncClient client;

  public WeaviateAsyncClient(Config config) {
    this.config = config;
    this.client = AsyncHttpClient.create(config);
    // auto start the client
    this.start();
  }

  public Misc misc() {
    return new Misc(client, config);
  }

  public Schema schema() {
    return new Schema(client, config);
  }

  private void start() {
    this.client.start();
  }

  @Override
  public void close() {
    this.client.close(CloseMode.GRACEFUL);
  }
}
