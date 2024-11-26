package io.weaviate.client.v1.async.cluster;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.cluster.api.NodesStatusGetter;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

@RequiredArgsConstructor
public class Cluster {

  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;


  public NodesStatusGetter nodesStatusGetter() {
    return new NodesStatusGetter(client, config, tokenProvider);
  }
}
