package io.weaviate.client.v1.graphql.query;

import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.builder.AggregateBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;

public class Aggregate extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private final AggregateBuilder.AggregateBuilderBuilder aggregateBuilder;

  public Aggregate(HttpClient httpClient, Config config) {
    super(httpClient, config);
    this.aggregateBuilder = AggregateBuilder.builder();
  }

  public Aggregate withClassName(String className) {
    this.aggregateBuilder.className(className);
    return this;
  }

  public Aggregate withFields(Field... fields) {
    this.aggregateBuilder.fields(Fields.builder().fields(fields).build());
    return this;
  }

  public Aggregate withWhere(WhereFilter where) {
    this.aggregateBuilder.withWhereFilter(where);
    return this;
  }

  public Aggregate withGroupBy(String propertyName) {
    this.aggregateBuilder.groupByClausePropertyName(propertyName);
    return this;
  }

  public Aggregate withNearVector(NearVectorArgument withNearVectorFilter) {
    this.aggregateBuilder.withNearVectorFilter(withNearVectorFilter);
    return this;
  }

  public Aggregate withNearObject(NearObjectArgument withNearObjectFilter) {
    this.aggregateBuilder.withNearObjectFilter(withNearObjectFilter);
    return this;
  }

  public Aggregate withNearText(NearTextArgument withNearTextFilter) {
    this.aggregateBuilder.withNearTextFilter(withNearTextFilter);
    return this;
  }

  public Aggregate withAsk(AskArgument ask) {
    this.aggregateBuilder.withAskArgument(ask);
    return this;
  }

  public Aggregate withObjectLimit(Integer objectLimit) {
    this.aggregateBuilder.objectLimit(objectLimit);
    return this;
  }

  @Override
  public Result<GraphQLResponse> run() {
    String aggregateQuery = aggregateBuilder.build().buildQuery();
    GraphQLQuery query = GraphQLQuery.builder().query(aggregateQuery).build();
    Response<GraphQLResponse> resp = sendPostRequest("/graphql", query, GraphQLResponse.class);
    return new Result<>(resp);
  }
}
