package io.weaviate.client.v1.async.graphql.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;

public class Raw extends AsyncBaseClient<GraphQLResponse> implements AsyncClientResult<GraphQLResponse> {
    private String query;

    public Raw(CloseableHttpAsyncClient client, Config config) {
        super(client, config);
    }

    public Raw withQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public Future<Result<GraphQLResponse>> run(FutureCallback<Result<GraphQLResponse>> callback) {
        GraphQLQuery query = GraphQLQuery.builder().query(this.query).build();
        return sendPostRequest("/graphql", query, GraphQLResponse.class, callback);
    }
}
