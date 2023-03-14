package io.weaviate.client.v1.graphql.query;

import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.builder.ExploreBuilder;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.graphql.model.ExploreFields;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;

public class Explore extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private final ExploreBuilder.ExploreBuilderBuilder exploreBuilder;

  public Explore(HttpClient httpClient, Config config) {
    super(httpClient, config);
    this.exploreBuilder = ExploreBuilder.builder();
  }

  public Explore withFields(ExploreFields... fields) {
    this.exploreBuilder.fields(fields);
    return this;
  }

  public Explore withLimit(Integer limit) {
    this.exploreBuilder.limit(limit);
    return this;
  }

  public Explore withOffset(Integer offset) {
    this.exploreBuilder.offset(offset);
    return this;
  }

  public Explore withNearVector(NearVectorArgument nearVector) {
    this.exploreBuilder.withNearVectorFilter(nearVector);
    return this;
  }

  public Explore withNearObject(NearObjectArgument nearObject) {
    this.exploreBuilder.withNearObjectFilter(nearObject);
    return this;
  }

  public Explore withNearText(NearTextArgument nearText) {
    this.exploreBuilder.withNearText(nearText);
    return this;
  }

  public Explore withAsk(AskArgument ask) {
    this.exploreBuilder.withAskArgument(ask);
    return this;
  }

  public Explore withNearImage(NearImageArgument nearImage) {
    this.exploreBuilder.withNearImageFilter(nearImage);
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
