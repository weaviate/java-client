package technology.semi.weaviate.client.v1.graphql.query;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.filters.WhereFilter;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLQuery;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.Bm25Argument;
import technology.semi.weaviate.client.v1.graphql.query.argument.HybridArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArguments;
import technology.semi.weaviate.client.v1.graphql.query.builder.GetBuilder;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

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

  public Get withFields(Field... fields) {
    this.getBuilder.fields(Fields.builder().fields(fields).build());
    return this;
  }

  public Get withWhere(WhereFilter where) {
    this.getBuilder.withWhereFilter(where);
    return this;
  }

  public Get withLimit(Integer limit) {
    this.getBuilder.limit(limit);
    return this;
  }

  public Get withOffset(Integer offset) {
    this.getBuilder.offset(offset);
    return this;
  }

  public Get withNearText(NearTextArgument nearText) {
    this.getBuilder.withNearTextFilter(nearText);
    return this;
  }

  public Get withBm25(Bm25Argument bm25) {
    this.getBuilder.withBm25Filter(bm25);
    return this;
  }

  public Get withHybrid(HybridArgument hybrid) {
    this.getBuilder.withHybridFilter(hybrid);
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

  public Get withNearVector(NearVectorArgument nearVector) {
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

  public Get withSort(SortArgument... sort) {
    this.getBuilder.withSortArguments(SortArguments.builder().sort(sort).build());
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
