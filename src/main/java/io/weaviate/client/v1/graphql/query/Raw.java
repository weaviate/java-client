package io.weaviate.client.v1.graphql.query;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;



public class Raw extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private  String query;
 
  public Raw(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public Raw withQuery (String query)  {
    this.query = query;
    return this;
  }

  @Override
  public Result<GraphQLResponse> run() {
    GraphQLQuery query = GraphQLQuery.builder().query(this.query).build();
    Response<GraphQLResponse> resp = sendPostRequest("/graphql", query, GraphQLResponse.class);
    return new Result<>(resp);
  }
}
