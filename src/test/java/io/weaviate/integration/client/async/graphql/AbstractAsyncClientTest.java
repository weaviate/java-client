package io.weaviate.integration.client.async.graphql;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.graphql.AbstractClientGraphQLTest;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AbstractAsyncClientTest extends AbstractClientGraphQLTest {
  static Field field(String name) {
    return Field.builder().name(name).build();
  }

  static Field[] fields(String... fieldNames) {
    Field[] fields = new Field[fieldNames.length];
    for (int i = 0; i < fieldNames.length; i++) {
      fields[i] = field(fieldNames[i]);
    }
    return fields;
  }

  static Field _additional(String... fieldNames) {
    return Field.builder().name("_additional").fields(fields(fieldNames)).build();
  }

  static Field meta(String... fieldNames) {
    return Field.builder().name("meta").fields(fields(fieldNames)).build();
  }


  static WhereArgument whereText(String property, String operator, String... valueText) {
    return WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(property)
        .operator(operator)
        .valueText(valueText)
        .build())
      .build();
  }

  static WhereArgument whereDate(String property, String operator, Date... valueDate) {
    return WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(property)
        .operator(operator)
        .valueDate(valueDate)
        .build())
      .build();
  }

  static WhereArgument whereNumber(String property, String operator, Double... valueNumber) {
    return WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(property)
        .operator(operator)
        .valueNumber(valueNumber)
        .build())
      .build();
  }

  /**
   * Check that request was processed successfully and no errors are returned. Extract the part of the response body for the specified query type.
   *
   * @param result    Result of a GraphQL query.
   * @param queryType "Get", "Explore", or "Aggregate".
   * @return "data" portion of the response
   */
  @SuppressWarnings("unchecked")
  <T> T extractQueryResult(Result<GraphQLResponse> result, String queryType) {
    assertNotNull(result, "graphQL request returned null");
    assertNull("GraphQL error in the response", result.getError());

    GraphQLResponse resp = result.getResult();
    assertNotNull(resp, "GraphQL response not returned");

    Map<String, Object> data = (Map<String, Object>) resp.getData();
    assertNotNull(data, "GraphQL response has no data");

    T queryResult = (T) data.get(queryType);
    assertNotNull(queryResult, String.format("%s query returned no result", queryType));

    return queryResult;
  }

  <T> T extractClass(Result<GraphQLResponse> result, String queryType, String className) {
    Map<String, T> queryResult = extractQueryResult(result, queryType);
    return extractClass(queryResult, className);
  }

  <T> T extractClass(Map<String, T> queryResult, String className) {
    T objects = queryResult.get(className);
    assertNotNull(objects, String.format("no %ss returned", className.toLowerCase()));
    return objects;
  }
}
