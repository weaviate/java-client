package io.weaviate.client.v1.async.cluster;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.cluster.api.NodesStatusGetter;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class Cluster {

  private final CloseableHttpAsyncClient client;
  private final Config config;

  public Cluster(CloseableHttpAsyncClient client, Config config) {
    this.client = client;
    this.config = config;
  }

  public NodesStatusGetter nodesStatusGetter() {
    return new NodesStatusGetter(client, config);
  }
}
