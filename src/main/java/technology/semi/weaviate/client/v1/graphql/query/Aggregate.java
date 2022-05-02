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
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import technology.semi.weaviate.client.v1.graphql.query.builder.AggregateBuilder;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

public class Aggregate extends BaseClient<GraphQLResponse> implements ClientResult<GraphQLResponse> {
  private final AggregateBuilder.AggregateBuilderBuilder aggregateBuilder;

  public Aggregate(Config config) {
    super(config);
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
