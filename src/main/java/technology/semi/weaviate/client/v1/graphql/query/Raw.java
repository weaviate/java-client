package technology.semi.weaviate.client.v1.graphql.query;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLQuery;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;



public class Raw extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private  String query;
 
  public Raw(Config config) {
    super(config);
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
