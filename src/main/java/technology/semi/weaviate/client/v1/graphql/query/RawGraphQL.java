package technology.semi.weaviate.client.v1.graphql.query;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLQuery;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.builder.RawGraphQLBuilder;


public class RawGraphQL extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private final RawGraphQLBuilder.RawGraphQLBuilderBuilder rawGraphQLBuilder;

  public RawGraphQL(Config config) {
    super(config);
    this.rawGraphQLBuilder = RawGraphQLBuilder.builder();
  }

  public RawGraphQL withQuery(String query) {
    this.rawGraphQLBuilder.query(query);
    return this;
  }


  @Override
  public Result<GraphQLResponse> run() {
    String getQuery = this.rawGraphQLBuilder.build().buildQuery();
    GraphQLQuery query = GraphQLQuery.builder().query(getQuery).build();
    Response<GraphQLResponse> resp = sendPostRequest("/graphql", query, GraphQLResponse.class);
    return new Result<>(resp);
  }
}
