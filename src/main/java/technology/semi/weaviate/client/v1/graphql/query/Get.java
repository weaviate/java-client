package technology.semi.weaviate.client.v1.graphql.query;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLQuery;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupArgument;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.WhereArgument;
import technology.semi.weaviate.client.v1.graphql.query.builder.GetBuilder;

public class Get extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private final GetBuilder.GetBuilderBuilder getBuilder;

  public Get(Config config) {
    super(config);
    this.getBuilder = GetBuilder.builder();
  }

  public Get withClassName(String className) {
    this.getBuilder.className(className);
    return this;
  }

  public Get withFields(Fields fields) {
    this.getBuilder.fields(fields);
    return this;
  }

  public Get withWhere(WhereArgument where) {
    this.getBuilder.withWhereArgument(where);
    return this;
  }

  public Get withLimit(Integer limit) {
    this.getBuilder.limit(limit);
    return this;
  }

  public Get withNearText(NearTextArgument nearText) {
    this.getBuilder.withNearTextFilter(nearText);
    return this;
  }

  public Get withAsk(AskArgument ask) {
    this.getBuilder.withAskArgument(ask);
    return this;
  }

  public Get withNearImage(NearImageArgument nearImage) {
    this.getBuilder.withNearImageFilter(nearImage);
    return this;
  }

  public Get withNearVector(Float[] nearVector) {
    this.getBuilder.withNearVectorFilter(nearVector);
    return this;
  }

  public Get withNearObject(NearObjectArgument nearObject) {
    this.getBuilder.withNearObjectFilter(nearObject);
    return this;
  }

  public Get withGroup(GroupArgument group) {
    this.getBuilder.withGroupArgument(group);
    return this;
  }

  @Override
  public Result<GraphQLResponse> run() {
    String getQuery = this.getBuilder.build().buildQuery();
    GraphQLQuery query = GraphQLQuery.builder().query(getQuery).build();
    Response<GraphQLResponse> resp = sendPostRequest("/graphql", query, GraphQLResponse.class);
    return new Result<>(resp);
  }
}
