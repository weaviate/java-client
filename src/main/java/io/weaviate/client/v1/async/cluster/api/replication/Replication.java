package io.weaviate.client.v1.async.cluster.api.replication;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.cluster.api.replication.api.ReplicationAllDeleter;
import io.weaviate.client.v1.async.cluster.api.replication.api.ReplicationAllGetter;
import io.weaviate.client.v1.async.cluster.api.replication.api.ReplicationCanceler;
import io.weaviate.client.v1.async.cluster.api.replication.api.ReplicationDeleter;
import io.weaviate.client.v1.async.cluster.api.replication.api.ReplicationGetter;
import io.weaviate.client.v1.async.cluster.api.replication.api.ReplicationQuerier;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Replication {

  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  public ReplicationGetter getter() {
    return new ReplicationGetter(client, config, tokenProvider);
  }

  public ReplicationAllGetter allGetter() {
    return new ReplicationAllGetter(client, config, tokenProvider);
  }

  public ReplicationQuerier querier() {
    return new ReplicationQuerier(client, config, tokenProvider);
  }

  public ReplicationCanceler canceler() {
    return new ReplicationCanceler(client, config, tokenProvider);
  }

  public ReplicationDeleter deleter() {
    return new ReplicationDeleter(client, config, tokenProvider);
  }

  public ReplicationAllDeleter allDeleter() {
    return new ReplicationAllDeleter(client, config, tokenProvider);
  }
}
