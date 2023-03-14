package io.weaviate.client.v1.graphql.query;

import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.Bm25Argument;
import io.weaviate.client.v1.graphql.query.argument.GroupArgument;
import io.weaviate.client.v1.graphql.query.argument.HybridArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArguments;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.v1.graphql.query.fields.GenerativeSearchBuilder;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;

public class Get extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private final GetBuilder.GetBuilderBuilder getBuilder;

  public Get(HttpClient httpClient, Config config) {
    super(httpClient, config);
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

  public Get withAfter(String after) {
    this.getBuilder.after(after);
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

  public Get withGenerativeSearch(GenerativeSearchBuilder generativeSearch) {
    this.getBuilder.withGenerativeSearch(generativeSearch);
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
