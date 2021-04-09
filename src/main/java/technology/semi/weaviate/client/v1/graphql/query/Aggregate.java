package technology.semi.weaviate.client.v1.graphql.query;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLQuery;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.builder.AggregateBuilder;

public class Aggregate extends BaseClient<GraphQLResponse> implements Client<GraphQLResponse> {
  private AggregateBuilder.AggregateBuilderBuilder aggregateBuilder;

  public Aggregate(Config config) {
    super(config);
    this.aggregateBuilder = AggregateBuilder.builder();
  }

  public Aggregate withClassName(String className) {
    this.aggregateBuilder.className(className);
    return this;
  }

  public Aggregate withFields(String fields) {
    this.aggregateBuilder.fields(fields);
    return this;
  }

  public Aggregate withGroupBy(String propertyName) {
    this.aggregateBuilder.groupByClausePropertyName(propertyName);
    return this;
  }

  @Override
  public GraphQLResponse run() {
    String aggregrateQuery = aggregateBuilder.build().buildQuery();
    GraphQLQuery query = GraphQLQuery.builder().query(aggregrateQuery).build();
    Response<GraphQLResponse> resp = sendPostRequest("/graphql", query, GraphQLResponse.class);
    if (resp.getStatusCode() == 200) {
      return resp.getBody();
    }
    return null;
  }
}
