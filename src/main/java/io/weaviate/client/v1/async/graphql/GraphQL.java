package io.weaviate.client.v1.async.graphql;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.graphql.api.Aggregate;
import io.weaviate.client.v1.async.graphql.api.Explore;
import io.weaviate.client.v1.async.graphql.api.Get;
import io.weaviate.client.v1.async.graphql.api.Raw;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class GraphQL {
  private final Config config;
  private final CloseableHttpAsyncClient client;
  private final AccessTokenProvider tokenProvider;

  public GraphQL(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    this.client = client;
    this.config = config;
    this.tokenProvider = tokenProvider;
  }

  public Get get() {
    return new Get(client, config, tokenProvider);
  }

  public Raw raw() {
    return new Raw(client, config, tokenProvider);
  }

  public Explore explore() {
    return new Explore(client, config, tokenProvider);
  }

  public Aggregate aggregate() {
    return new Aggregate(client, config, tokenProvider);
  }

  public io.weaviate.client.v1.graphql.GraphQL.Arguments arguments() {
    return new io.weaviate.client.v1.graphql.GraphQL.Arguments();
  }
}
