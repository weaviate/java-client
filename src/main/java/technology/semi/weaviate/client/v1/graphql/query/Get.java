package technology.semi.weaviate.client.v1.graphql.query;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLQuery;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.builder.GetBuilder;

public class Get extends BaseClient<GraphQLResponse> implements Client<GraphQLResponse> {
  private GetBuilder.GetBuilderBuilder getBuilder;

  public Get(Config config) {
    super(config);
    this.getBuilder = GetBuilder.builder();
  }

  public Get withClassName(String className) {
    this.getBuilder.className(className);
    return this;
  }

  public Get withFields(String fields) {
    this.getBuilder.fields(fields);
    return this;
  }

  public Get withWhere(String filter) {
    this.getBuilder.withWhereFilter(filter);
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

  public Get withNearVector(Float[] nearVector) {
    this.getBuilder.withNearVectorFilter(nearVector);
    return this;
  }

  public Get withNearObject(NearObjectArgument nearObject) {
    this.getBuilder.withNearObjectFilter(nearObject);
    return this;
  }

  public Get withGroup(String group) {
    this.getBuilder.withGroupFilter(group);
    return this;
  }

  @Override
  public GraphQLResponse run() {
    String getQuery = this.getBuilder.build().buildQuery();
    GraphQLQuery query = GraphQLQuery.builder().query(getQuery).build();
    Response<GraphQLResponse> resp = sendPostRequest("/graphql", query, GraphQLResponse.class);
    if (resp.getStatusCode() == 200) {
      return resp.getBody();
    }
    return null;
  }
}
