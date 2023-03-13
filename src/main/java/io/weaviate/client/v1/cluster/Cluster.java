package io.weaviate.client.v1.cluster;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.cluster.api.NodesStatusGetter;
import io.weaviate.client.Config;

public class Cluster {

  private final Config config;
  private final HttpClient httpClient;

  public Cluster(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public NodesStatusGetter nodesStatusGetter() {
    return new NodesStatusGetter(httpClient, config);
  }
}
