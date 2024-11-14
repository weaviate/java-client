package io.weaviate.client.v1.async.graphql;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.graphql.api.Aggregate;
import io.weaviate.client.v1.async.graphql.api.Explore;
import io.weaviate.client.v1.async.graphql.api.Get;
import io.weaviate.client.v1.async.graphql.api.Raw;

public class GraphQL {
    private final Config config;
    private final CloseableHttpAsyncClient client;

    public GraphQL(CloseableHttpAsyncClient client, Config config) {
        this.client = client;
        this.config = config;
    }

    public Get get() {
        return new Get(client, config);
    }

    public Raw raw() {
        return new Raw(client, config);
    }

    public Explore explore() {
        return new Explore(client, config);
    }

    public Aggregate aggregate() {
        return new Aggregate(client, config);
    }

    public io.weaviate.client.v1.graphql.GraphQL.Arguments arguments() {
        return new io.weaviate.client.v1.graphql.GraphQL.Arguments();
    }
}
