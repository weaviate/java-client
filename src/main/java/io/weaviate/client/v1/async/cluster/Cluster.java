package io.weaviate.client.v1.async.cluster;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.cluster.api.NodesStatusGetter;
import io.weaviate.client.v1.async.cluster.api.Replicator;
import io.weaviate.client.v1.async.cluster.api.ShardingStateQuerier;
import io.weaviate.client.v1.async.cluster.api.replication.Replication;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Cluster {

  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  public Replication replication() {
    return new Replication(client, config, tokenProvider);
  }

  public NodesStatusGetter nodesStatusGetter() {
    return new NodesStatusGetter(client, config, tokenProvider);
  }

  public Replicator replicator() {
    return new Replicator(client, config, tokenProvider);
  }

  public ShardingStateQuerier shardingStateQuerier() {
    return new ShardingStateQuerier(client, config, tokenProvider);
  }
}
