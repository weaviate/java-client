package technology.semi.weaviate.client.v1.graphql;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.graphql.query.Aggregate;
import technology.semi.weaviate.client.v1.graphql.query.Explore;
import technology.semi.weaviate.client.v1.graphql.query.Get;

public class GraphQL {
  private Config config;

  public GraphQL(Config config) {
    this.config = config;
  }

  public Get get() {
    return new Get(config);
  }

  public Explore explore() {
    return new Explore(config);
  }

  public Aggregate aggregrate() {
    return new Aggregate(config);
  }
}
