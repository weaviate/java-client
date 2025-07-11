package io.weaviate.client.v1.cluster.api.replication;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.cluster.api.replication.api.ReplicationAllDeleter;
import io.weaviate.client.v1.cluster.api.replication.api.ReplicationAllGetter;
import io.weaviate.client.v1.cluster.api.replication.api.ReplicationCanceler;
import io.weaviate.client.v1.cluster.api.replication.api.ReplicationDeleter;
import io.weaviate.client.v1.cluster.api.replication.api.ReplicationGetter;
import io.weaviate.client.v1.cluster.api.replication.api.ReplicationQuerier;

public class Replication {

  private final Config config;
  private final HttpClient httpClient;

  public Replication(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public ReplicationGetter getter() {
    return new ReplicationGetter(httpClient, config);
  }

  public ReplicationAllGetter allGetter() {
    return new ReplicationAllGetter(httpClient, config);
  }

  public ReplicationQuerier querier() {
    return new ReplicationQuerier(httpClient, config);
  }

  public ReplicationCanceler canceler() {
    return new ReplicationCanceler(httpClient, config);
  }

  public ReplicationDeleter deleter() {
    return new ReplicationDeleter(httpClient, config);
  }

  public ReplicationAllDeleter allDeleter() {
    return new ReplicationAllDeleter(httpClient, config);
  }
}
