package technology.semi.weaviate.client.v1.cluster;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.cluster.api.NodesStatusGetter;

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
