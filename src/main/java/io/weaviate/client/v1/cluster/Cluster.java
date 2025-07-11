package io.weaviate.client.v1.cluster;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.cluster.api.NodesStatusGetter;
import io.weaviate.client.v1.cluster.api.Replicator;
import io.weaviate.client.v1.cluster.api.ShardingStateQuerier;
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

  public ShardingStateQuerier shardingStateQuerier() {
    return new ShardingStateQuerier(httpClient, config);
  }

  public Replicator replicator() {
    return new Replicator(httpClient, config);
  }
}
