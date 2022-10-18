package technology.semi.weaviate.client.v1.cluster;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.cluster.api.NodesStatusGetter;

public class Cluster {

  private final Config config;

  public Cluster(Config config) {
    this.config = config;
  }

  public NodesStatusGetter nodesStatusGetter() {
    return new NodesStatusGetter(config);
  }
}
