package io.weaviate.integration.client.async.graphql;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.graphql.GraphQL;
import io.weaviate.client.v1.async.graphql.api.Aggregate;
import io.weaviate.client.v1.async.graphql.api.Explore;
import io.weaviate.client.v1.async.graphql.api.Get;
import io.weaviate.client.v1.async.graphql.api.Raw;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.ExploreFields;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.*;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

public class ClientGraphQLTest {
  private String address;
  private String openAIApiKey;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
  ;

  private WeaviateClient syncClient;
  private WeaviateAsyncClient client;
  private GraphQL gql;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();
    openAIApiKey = System.getenv("OPENAI_APIKEY");

    syncClient = new WeaviateClient(new Config("http", address));
    testGenerics.createTestSchemaAndData(syncClient);

    client = syncClient.async();
    gql = client.graphQL();
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(syncClient);
    client.close();
  }

  @Test
  public void testGraphQLGet() {
    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withFields(field("name")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(4, pizzas.size(), "wrong number of pizzas returned");
  }

  @Test
  public void testGraphQLRaw() {
    String query = "{Get{Pizza{_additional{id}}}}";

    Result<GraphQLResponse> result = doRaw(raw -> raw.withQuery(query));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(4, pizzas.size(), "wrong number of pizzas returned");
  }

  @Test
  public void testGraphQLGetWithNearObjectAndCertainty() {
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    NearObjectArgument nearObjectArgument = gql.arguments()
      .nearObjectArgBuilder()
      .id(newObjID)
      .certainty(0.99f)
      .build();

    WeaviateObject soupWithID = WeaviateObject.builder()
      .className("Soup")
      .id(newObjID)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustSoup");
          put("description", "soup with id");
        }
      })
      .build();

    // Insert additional test data
    Result<ObjectGetResponse[]> insert = syncClient.batch()
      .objectsBatcher()
      .withObjects(soupWithID)
      .run();
    assumeTrue("all test objects inserted successfully", insert.getResult().length == 1);

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Soup")
      .withNearObject(nearObjectArgument)
      .withFields(field("name"), _additional("certainty")));

    List<?> soups = extractClass(result, "Get", "Soup");
    assertEquals(1, soups.size(), "wrong number of soups");
  }

  @Test
  public void testGraphQLGetWithNearObjectAndDistance() {
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    NearObjectArgument nearObjectArgument = gql.arguments()
      .nearObjectArgBuilder()
      .id(newObjID)
      .distance(0.1f)
      .build();

    WeaviateObject soupWithID = WeaviateObject.builder()
      .className("Soup")
      .id(newObjID)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustSoup");
          put("description", "soup with id");
        }
      })
      .build();

    // Insert additional test data
    syncClient.batch()
      .objectsBatcher()
      .withObjects(soupWithID)
      .run();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Soup")
      .withNearObject(nearObjectArgument)
      .withFields(field("name"), _additional("distance")));

    List<?> soups = extractClass(result, "Get", "Soup");
    assertEquals(1, soups.size(), "wrong number of soups");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testBm25() {
    Bm25Argument bm25 = gql.arguments()
      .bm25ArgBuilder()
      .query("innovation")
      .properties(new String[]{ "description" })
      .build();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withBm25(bm25)
      .withFields(field("description"), _additional("id", "distance")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(1, pizzas.size(), "wrong number of pizzas");

    Map<String, String> pizza = (Map<String, String>) pizzas.get(0);
    assertTrue(((String) pizza.get("description")).contains("innovation"), "wrong Pizza description");
  }

  @Test
  public void testHybrid() {
    HybridArgument hybrid = gql.arguments()
      .hybridArgBuilder()
      .query("some say revolution")
      .alpha(0.8f)
      .build();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withHybrid(hybrid)
      .withFields(field("description"), _additional("id")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertFalse(pizzas.isEmpty(), "didn't get any pizzas");
  }

  @Test
  public void testGraphQLGetWithNearTextAndCertainty() {
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
      .concepts(new String[]{ "Universally" })
      .force(0.8f)
      .build();
    NearTextArgument nearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(new String[]{ "some say revolution" })
      .moveAwayFrom(moveAway)
      .certainty(0.8f)
      .build();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withNearText(nearText)
      .withFields(field("name"), _additional("certainty")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(1, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetWithNearTextAndDistance() {
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
      .concepts(new String[]{ "Universally" })
      .force(0.8f)
      .build();
    NearTextArgument nearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(new String[]{ "some say revolution" })
      .moveAwayFrom(moveAway)
      .distance(0.4f)
      .build();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withNearText(nearText)
      .withFields(field("name"), _additional("distance")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(1, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetWithNearTextAndMoveParamsAndCertainty() {
    String newObjID1 = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    String newObjID2 = "6baed48e-2afe-4be4-a09d-b00a955d962a";
    WeaviateObject pizzaWithID = WeaviateObject.builder()
      .className("Pizza")
      .id(newObjID1)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustPizza1");
          put("description", "Universally pizza with id");
        }
      })
      .build();
    WeaviateObject pizzaWithID2 = WeaviateObject.builder()
      .className("Pizza")
      .id(newObjID2)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustPizza2");
          put("description", "Universally pizza with some other id");
        }
      })
      .build();

    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{ NearTextMoveParameters.ObjectMove.builder()
        .id(newObjID1).build()
      })
      .force(0.9f)
      .build();
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{ NearTextMoveParameters.ObjectMove.builder()
        .id(newObjID2).build()
      })
      .force(0.9f)
      .build();
    NearTextArgument nearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(new String[]{ "Universally pizza with id" })
      .moveAwayFrom(moveAway)
      .moveTo(moveTo)
      .certainty(0.4f)
      .build();

    // Insert additional test data
    Result<ObjectGetResponse[]> insert = syncClient.batch()
      .objectsBatcher()
      .withObjects(pizzaWithID, pizzaWithID2)
      .run();
    assumeTrue("all test objects inserted successfully", insert.getResult().length == 2);

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withNearText(nearText)
      .withFields(field("name"), _additional("certainty")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(6, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetWithNearTextAndMoveParamsAndDistance() {
    String newObjID1 = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    String newObjID2 = "6baed48e-2afe-4be4-a09d-b00a955d962a";
    WeaviateObject pizzaWithID = WeaviateObject.builder()
      .className("Pizza")
      .id(newObjID1)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustPizza1");
          put("description", "Universally pizza with id");
        }
      })
      .build();
    WeaviateObject pizzaWithID2 = WeaviateObject.builder()
      .className("Pizza")
      .id(newObjID2)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustPizza2");
          put("description", "Universally pizza with some other id");
        }
      })
      .build();

    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{ NearTextMoveParameters.ObjectMove.builder()
        .id(newObjID1).build()
      })
      .force(0.9f)
      .build();
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{ NearTextMoveParameters.ObjectMove.builder()
        .id(newObjID2).build()
      })
      .force(0.9f)
      .build();
    NearTextArgument nearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(new String[]{ "Universally pizza with id" })
      .moveAwayFrom(moveAway)
      .moveTo(moveTo)
      .distance(0.6f)
      .build();

    // Insert additional test data
    Result<ObjectGetResponse[]> insert = syncClient.batch()
      .objectsBatcher()
      .withObjects(pizzaWithID, pizzaWithID2)
      .run();
    assumeTrue("all test objects inserted successfully", insert.getResult().length == 2);

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withNearText(nearText)
      .withFields(field("name"), _additional("distance")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(6, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetWithNearTextAndLimitAndCertainty() {
    NearTextArgument nearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(new String[]{ "some say revolution" })
      .certainty(0.8f)
      .build();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withNearText(nearText)
      .withLimit(1)
      .withFields(field("name"), _additional("certainty")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(1, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetWithNearTextAndLimitAndDistance() {
    NearTextArgument nearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(new String[]{ "some say revolution" })
      .distance(0.4f)
      .build();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withNearText(nearText)
      .withLimit(1)
      .withFields(field("name"), _additional("distance")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(1, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetWithWhereByFieldTokenizedProperty() {
    Field name = field("name");
    WhereArgument whereFullString = whereText("name", Operator.Equal, "Frutti di Mare");
    WhereArgument wherePartString = whereText("name", Operator.Equal, "Frutti");
    WhereArgument whereFullText = whereText("description", Operator.Equal, "Universally accepted to be the best pizza ever created.");
    WhereArgument wherePartText = whereText("description", Operator.Equal, "Universally");
    // when
    Result<GraphQLResponse> resultFullString = doGet(get -> get.withWhere(whereFullString)
      .withClassName("Pizza")
      .withFields(name));
    Result<GraphQLResponse> resultPartString = doGet(get -> get.withWhere(wherePartString)
      .withClassName("Pizza")
      .withFields(name));
    Result<GraphQLResponse> resultFullText = doGet(get -> get.withWhere(whereFullText)
      .withClassName("Pizza")
      .withFields(name));
    Result<GraphQLResponse> resultPartText = doGet(get -> get.withWhere(wherePartText)
      .withClassName("Pizza")
      .withFields(name));
    // then
    assertWhereResultSize(1, resultFullString, "Pizza");
    assertWhereResultSize(0, resultPartString, "Pizza");
    assertWhereResultSize(1, resultFullText, "Pizza");
    assertWhereResultSize(1, resultPartText, "Pizza");
  }

  @Test
  public void shouldSupportDeprecatedValueString() {
    WhereArgument whereString = whereText("name", Operator.Equal, "Frutti di Mare");

    Result<GraphQLResponse> result = doGet(get -> get.withWhere(whereString)
      .withClassName("Pizza")
      .withFields(field("name")));

    assertWhereResultSize(1, result, "Pizza");
  }

  @Test
  public void testGraphQLGetWithWhereByDate() {
    Calendar cal = Calendar.getInstance();
    cal.set(2022, Calendar.FEBRUARY, 1, 0, 0, 0);
    WhereArgument whereDate = whereDate("bestBefore", Operator.GreaterThan, cal.getTime());

    Result<GraphQLResponse> resultDate = doGet(get -> get.withWhere(whereDate)
      .withClassName("Pizza")
      .withFields(field("name")));

    List<Map<String, Object>> result = extractClass(resultDate, "Get", "Pizza");
    Assertions.assertThat(result)
      .hasSize(3)
      .extracting(el -> (String) el.get("name"))
      .contains("Frutti di Mare", "Hawaii", "Doener");
  }

  @Test
  public void testGraphQLExploreWithCertainty() {
    ExploreFields[] fields = new ExploreFields[]{ ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.CLASS_NAME };
    String[] concepts = new String[]{ "pineapple slices", "ham" };
    NearTextMoveParameters moveTo = gql.arguments()
      .nearTextMoveParameterBuilder()
      .concepts(new String[]{ "Pizza" })
      .force(0.3f)
      .build();
    NearTextMoveParameters moveAwayFrom = gql.arguments()
      .nearTextMoveParameterBuilder()
      .concepts(new String[]{ "toast", "bread" })
      .force(0.4f)
      .build();
    NearTextArgument withNearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(concepts)
      .certainty(0.40f)
      .moveTo(moveTo)
      .moveAwayFrom(moveAwayFrom)
      .build();

    Result<GraphQLResponse> result = doExplore(explore -> explore.withFields(fields)
      .withNearText(withNearText));

    List<?> got = extractQueryResult(result, "Explore");
    assertEquals(6, got.size());
  }

  @Test
  public void testGraphQLExploreWithDistance() {
    ExploreFields[] fields = new ExploreFields[]{ ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.CLASS_NAME };
    String[] concepts = new String[]{ "pineapple slices", "ham" };
    NearTextMoveParameters moveTo = gql.arguments()
      .nearTextMoveParameterBuilder()
      .concepts(new String[]{ "Pizza" })
      .force(0.3f)
      .build();
    NearTextMoveParameters moveAwayFrom = gql.arguments()
      .nearTextMoveParameterBuilder()
      .concepts(new String[]{ "toast", "bread" })
      .force(0.4f)
      .build();
    NearTextArgument withNearText = gql.arguments()
      .nearTextArgBuilder()
      .concepts(concepts)
      .distance(0.80f)
      .moveTo(moveTo)
      .moveAwayFrom(moveAwayFrom)
      .build();

    Result<GraphQLResponse> result = doExplore(explore -> explore.withFields(fields)
      .withNearText(withNearText));

    List<?> got = extractQueryResult(result, "Explore");
    assertEquals(6, got.size());
  }

  @Test
  public void testGraphQLAggregate() {
    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withFields(meta("count"))
      .withClassName("Pizza"));

    checkAggregateMetaCount(result, "Pizza", 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithWhereFilter() {
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d96ee";
    WeaviateObject pizzaWithID = WeaviateObject.builder()
      .className("Pizza")
      .id(newObjID)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustPizza");
          put("description", "pizza with id");
        }
      })
      .build();

    // Insert additional test data
    Result<ObjectGetResponse[]> insert = syncClient.batch()
      .objectsBatcher()
      .withObjects(pizzaWithID)
      .run();
    assumeTrue("all test objects inserted successfully", insert.getResult().length == 1);

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withFields(meta("count"))
      .withClassName("Pizza")
      .withWhere(whereText("id", Operator.Equal, newObjID)));

    checkAggregateMetaCount(result, "Pizza", 1, 1.0d);
  }

  @Test
  public void testGraphQLAggregateWithGroupedByAndWhere() {
    // given
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d96ee";
    WeaviateObject pizzaWithID = WeaviateObject.builder()
      .className("Pizza")
      .id(newObjID)
      .properties(new HashMap<String, java.lang.Object>() {
        {
          put("name", "JustPizza");
          put("description", "pizza with id");
        }
      })
      .build();

    // Insert additional test objects
    Result<ObjectGetResponse[]> insert = syncClient.batch()
      .objectsBatcher()
      .withObjects(pizzaWithID)
      .run();
    assumeTrue("all test objects inserted successfully", insert.getResult().length == 1);

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withFields(meta("count"))
      .withClassName("Pizza")
      .withGroupBy("name")
      .withWhere(whereText("id", Operator.Equal, newObjID)));

    checkAggregateMetaCount(result, "Pizza", 1, 1.0d);
  }

  private Result<GraphQLResponse> doGet(Consumer<Get> build) {
    Get get = gql.get();
    build.accept(get);
    try {
      return get.run()
        .get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.get(): " + e.getMessage());
      return null;
    }
  }

  private Result<GraphQLResponse> doRaw(Consumer<Raw> build) {
    Raw raw = gql.raw();
    build.accept(raw);
    try {
      return raw.run()
        .get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.raw(): " + e.getMessage());
      return null;
    }
  }

  private Result<GraphQLResponse> doExplore(Consumer<Explore> build) {
    Explore explore = gql.explore();
    build.accept(explore);
    try {
      return explore.run()
        .get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.explore(): " + e.getMessage());
      return null;
    }
  }

  private Result<GraphQLResponse> doAggregate(Consumer<Aggregate> build) {
    Aggregate aggregate = gql.aggregate();
    build.accept(aggregate);
    try {
      return aggregate.run()
        .get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.aggregate(): " + e.getMessage());
      return null;
    }
  }

  /**
   * Check that request was processed successfully and no errors are returned. Extract the part of the response body for the specified query type.
   *
   * @param result    Result of a GraphQL query.
   * @param queryType "Get", "Explore", or "Aggregate".
   * @return "data" portion of the response
   */
  @SuppressWarnings("unchecked")
  private <T> T extractQueryResult(Result<GraphQLResponse> result, String queryType) {
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

  private <T> T extractClass(Result<GraphQLResponse> result, String queryType, String className) {
    Map<String, T> queryResult = extractQueryResult(result, queryType);
    return extractClass(queryResult, className);
  }

  private <T> T extractClass(Map<String, T> queryResult, String className) {
    T objects = queryResult.get(className);
    assertNotNull(objects, String.format("no %ss returned", className.toLowerCase()));
    return objects;
  }

  private static Field field(String name) {
    return Field.builder()
      .name(name)
      .build();
  }

  private static Field[] fields(String... fieldNames) {
    Field[] fields = new Field[fieldNames.length];
    for (int i = 0; i < fieldNames.length; i++) {
      fields[i] = field(fieldNames[i]);
    }
    return fields;
  }

  private static Field _additional(String... fieldNames) {
    return Field.builder()
      .name("_additional")
      .fields(fields(fieldNames))
      .build();
  }

  private static Field meta(String... fieldNames) {
    return Field.builder()
      .name("meta")
      .fields(fields(fieldNames))
      .build();
  }

  private static WhereArgument whereText(String property, String operator, String... valueText) {
    return WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(property)
        .operator(operator)
        .valueText(valueText)
        .build())
      .build();
  }

  private static WhereArgument whereDate(String property, String operator, Date... valueDate) {
    return WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(property)
        .operator(operator)
        .valueDate(valueDate)
        .build())
      .build();
  }

  private void assertWhereResultSize(int expectedSize, Result<GraphQLResponse> result, String className) {
    List<?> getClass = extractClass(result, "Get", className);
    assertEquals(expectedSize, getClass.size());
  }

  @SuppressWarnings("unchecked")
  private void checkAggregateMetaCount(Result<GraphQLResponse> result, String className, int wantObjects, Double wantCount) {
    List<?> objects = extractClass(result, "Aggregate", className);

    assertEquals(wantObjects, objects.size(), "wrong number of objects");
    Map<String, Map<String, Object>> firstObject = (Map<String, Map<String, Object>>) objects.get(0);
    Map<String, Object> meta = firstObject.get("meta");
    assertEquals(wantCount, meta.get("count"), "wrong meta:count");
  }
}
