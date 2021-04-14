package technology.semi.weaviate.client.v1.graphql.query;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.graphql.model.ExploreFields;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLQuery;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.builder.ExploreBuilder;

public class Explore extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private ExploreBuilder.ExploreBuilderBuilder exploreBuilder;

  public Explore(Config config) {
    super(config);
    this.exploreBuilder = ExploreBuilder.builder();
  }

  public Explore withFields(ExploreFields[] fields) {
    this.exploreBuilder.fields(fields);
    return this;
  }

  public Explore withNearText(NearTextArgument nearText) {
    this.exploreBuilder.withNearText(nearText);
    return this;
  }

  @Override
  public Result<GraphQLResponse> run() {
    String exploreQuery = exploreBuilder.build().buildQuery();
    GraphQLQuery query = GraphQLQuery.builder().query(exploreQuery).build();
    Response<GraphQLResponse> resp = sendPostRequest("/graphql", query, GraphQLResponse.class);
    return new Result<>(resp);
  }
}