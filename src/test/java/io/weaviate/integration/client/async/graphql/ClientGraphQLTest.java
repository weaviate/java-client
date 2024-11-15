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
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
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

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

public class ClientGraphQLTest extends AbstractAsyncClientTest {
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
  private final WeaviateTestGenerics.DocumentPassageSchema passageSchema = new WeaviateTestGenerics.DocumentPassageSchema();

  private String address;
  private WeaviateClient syncClient;
  private WeaviateAsyncClient client;
  private GraphQL gql;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();

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

    assertAggregateMetaCount(result, "Pizza", 1, 4.0d);
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

    assertAggregateMetaCount(result, "Pizza", 1, 1.0d);
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

    assertAggregateMetaCount(result, "Pizza", 1, 1.0d);
  }

  @Test
  public void testGraphQLAggregateWithGroupedBy() {
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

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withClassName("Pizza")
      .withFields(meta("count"))
      .withGroupBy("name"));

    assertAggregateMetaCount(result, "Pizza", 5, 1.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearVector() {
    Result<GraphQLResponse> getVector = doGet(get -> get.withClassName("Pizza")
      .withFields(_additional("vector")));
    Float[] vector = extractVector(getVector, "Get", "Pizza");
    NearVectorArgument nearVector = NearVectorArgument.builder()
      .certainty(0.7f)
      .vector(vector)
      .build();

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withClassName("Pizza")
      .withFields(meta("count"))
      .withNearVector(nearVector));

    assertAggregateMetaCount(result, "Pizza", 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearObjectAndCertainty() {
    Result<GraphQLResponse> getId = doGet(get -> get.withClassName("Pizza")
      .withFields(_additional("id")));
    String id = extractAdditional(getId, "Get", "Pizza", "id");

    // when
    NearObjectArgument nearObject = NearObjectArgument.builder()
      .certainty(0.7f)
      .id(id)
      .build();
    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withClassName("Pizza")
      .withFields(meta("count"))
      .withNearObject(nearObject));

    assertAggregateMetaCount(result, "Pizza", 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearObjectAndDistance() {
    Result<GraphQLResponse> getId = doGet(get -> get.withClassName("Pizza")
      .withFields(_additional("id")));
    String id = extractAdditional(getId, "Get", "Pizza", "id");

    NearObjectArgument nearObject = NearObjectArgument.builder()
      .distance(0.3f)
      .id(id)
      .build();
    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withFields(meta("count"))
      .withClassName("Pizza")
      .withNearObject(nearObject));

    assertAggregateMetaCount(result, "Pizza", 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearTextAndCertainty() {
    NearTextArgument nearText = NearTextArgument.builder()
      .certainty(0.7f)
      .concepts(new String[]{ "pizza" })
      .build();

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withClassName("Pizza")
      .withFields(meta("count"))
      .withNearText(nearText));

    assertAggregateMetaCount(result, "Pizza", 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearTextAndDistance() {
    NearTextArgument nearText = NearTextArgument.builder()
      .distance(0.6f)
      .concepts(new String[]{ "pizza" })
      .build();

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withClassName("Pizza")
      .withFields(meta("count"))
      .withNearText(nearText));

    assertAggregateMetaCount(result, "Pizza", 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithObjectLimitAndCertainty() {
    int limit = 1;
    NearTextArgument nearText = NearTextArgument.builder()
      .certainty(0.7f)
      .concepts(new String[]{ "pizza" })
      .build();

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withClassName("Pizza")
      .withFields(meta("count"))
      .withNearText(nearText)
      .withObjectLimit(limit));

    assertAggregateMetaCount(result, "Pizza", 1, (double) limit);
  }

  @Test
  public void testGraphQLAggregateWithObjectLimitAndDistance() {
    int limit = 1;
    NearTextArgument nearText = NearTextArgument.builder()
      .distance(0.3f)
      .concepts(new String[]{ "pizza" })
      .build();

    Result<GraphQLResponse> result = doAggregate(aggregate -> aggregate.withClassName("Pizza")
      .withFields(meta("count"))
      .withNearText(nearText)
      .withObjectLimit(limit));

    assertAggregateMetaCount(result, "Pizza", 1, (double) limit);
  }

  @Test
  public void testGraphQLGetWithGroup() {
    GroupArgument group = gql.arguments()
      .groupArgBuilder()
      .type(GroupType.merge)
      .force(1.0f)
      .build();

    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Soup")
      .withFields(field("name"))
      .withGroup(group)
      .withLimit(7));

    List<?> soups = extractClass(result, "Get", "Soup");
    assertEquals(1, soups.size(), "wrong number of soups");
  }

  @Test
  public void testGraphQLGetWithSort() {
    SortArgument byNameDesc = sort(SortOrder.desc, "name");
    String[] expectedByNameDesc = new String[]{ "Quattro Formaggi", "Hawaii", "Frutti di Mare", "Doener" };

    SortArgument byPriceAsc = sort(SortOrder.asc, "price");
    String[] expectedByPriceAsc = new String[]{ "Hawaii", "Doener", "Quattro Formaggi", "Frutti di Mare" };

    Field name = field("name");

    Result<GraphQLResponse> resultByNameDesc = doGet(get -> get.withClassName("Pizza")
      .withSort(byNameDesc)
      .withFields(name));
    Result<GraphQLResponse> resultByDescriptionAsc = doGet(get -> get.withClassName("Pizza")
      .withSort(byPriceAsc)
      .withFields(name));
    Result<GraphQLResponse> resultByNameDescByPriceAsc = doGet(get -> get.withClassName("Pizza")
      .withSort(byNameDesc, byPriceAsc)
      .withFields(name));

    assertObjectNamesEqual(resultByNameDesc, "Get", "Pizza", expectedByNameDesc);
    assertObjectNamesEqual(resultByDescriptionAsc, "Get", "Pizza", expectedByPriceAsc);
    assertObjectNamesEqual(resultByNameDescByPriceAsc, "Get", "Pizza", expectedByNameDesc);
  }

  @Test
  public void testGraphQLGetWithTimestampFilters() {
    Field additional = _additional("id", "creationTimeUnix", "lastUpdateTimeUnix");
    Result<GraphQLResponse> expected = doGet(get -> get.withClassName("Pizza")
      .withFields(additional));

    String expectedCreateTime = extractAdditional(expected, "Get", "Pizza", "creationTimeUnix");
    String expectedUpdateTime = extractAdditional(expected, "Get", "Pizza", "lastUpdateTimeUnix");

    Result<GraphQLResponse> createTimeResult = doGet(get -> get.withClassName("Pizza")
      .withWhere(whereText("_creationTimeUnix", Operator.Equal, expectedCreateTime))
      .withFields(additional));
    Result<GraphQLResponse> updateTimeResult = doGet(get -> get.withClassName("Pizza")
      .withWhere(whereText("_lastUpdateTimeUnix", Operator.Equal, expectedCreateTime))
      .withFields(additional));

    String resultCreateTime = extractAdditional(createTimeResult, "Get", "Pizza", "creationTimeUnix");
    assertEquals(expectedCreateTime, resultCreateTime);

    String resultUpdateTime = extractAdditional(updateTimeResult, "Get", "Pizza", "lastUpdateTimeUnix");
    assertEquals(expectedUpdateTime, resultUpdateTime);
  }

  @Test
  public void testGraphQLGetUsingCursorAPI() {
    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withAfter("00000000-0000-0000-0000-000000000000")
      .withLimit(10)
      .withFields(field("name")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(3, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetUsingLimitAndOffset() {
    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza")
      .withOffset(3)
      .withLimit(4)
      .withFields(field("name")));

    List<?> pizzas = extractClass(result, "Get", "Pizza");
    assertEquals(1, pizzas.size(), "wrong number of pizzas");
  }

  @Test
  public void testGraphQLGetWithGroupBy() {
    Field[] hits = new Field[]{ Field.builder()
      .name("ofDocument")
      .fields(new Field[]{ Field.builder()
        .name("... on Document")
        .fields(new Field[]{ Field.builder()
          .name("_additional{id}").build() }).build()
      }).build(), Field.builder()
      .name("_additional{id distance}").build(),
    };

    Field group = Field.builder()
      .name("group")
      .fields(new Field[]{ Field.builder()
        .name("id").build(), Field.builder()
        .name("groupedBy")
        .fields(new Field[]{ Field.builder()
          .name("value").build(), Field.builder()
          .name("path").build(),
        }).build(), Field.builder()
        .name("count").build(), Field.builder()
        .name("maxDistance").build(), Field.builder()
        .name("minDistance").build(), Field.builder()
        .name("hits")
        .fields(hits).build(),
      })
      .build();

    Field _additional = Field.builder()
      .name("_additional")
      .fields(new Field[]{ group })
      .build();
    Field ofDocument = Field.builder()
      .name("ofDocument{__typename}")
      .build(); // Property that we group by

    GroupByArgument groupBy = client.graphQL()
      .arguments()
      .groupByArgBuilder()
      .path(new String[]{ "ofDocument" })
      .groups(3)
      .objectsPerGroup(10)
      .build();
    NearObjectArgument nearObject = client.graphQL()
      .arguments()
      .nearObjectArgBuilder()
      .id("00000000-0000-0000-0000-000000000001")
      .build();

    passageSchema.createAndInsertData(syncClient);

    try {
      Result<GraphQLResponse> result = doGet(get -> get.withClassName(passageSchema.PASSAGE)
        .withNearObject(nearObject)
        .withGroupBy(groupBy)
        .withFields(ofDocument, _additional));

      List<Map<String, Object>> passages = extractClass(result, "Get", passageSchema.PASSAGE);
      assertEquals(3, passages.size(), "wrong number of passages");

      // This part of assertions is almost verbatim from package io.weaviate.integration.client.graphql.ClientGraphQLTest
      // because it involves a lot of inner classes that we don't won't to redefine here.
      List<Group> groups = getGroups(passages);
      Assertions.assertThat(groups)
        .isNotNull()
        .hasSize(3);
      for (int i = 0; i < 3; i++) {
        Assertions.assertThat(groups.get(i).minDistance)
          .isEqualTo(groups.get(i)
            .getHits()
            .get(0)
            .get_additional()
            .getDistance());
        Assertions.assertThat(groups.get(i).maxDistance)
          .isEqualTo(groups.get(i)
            .getHits()
            .get(groups.get(i)
              .getHits()
              .size() - 1)
            .get_additional()
            .getDistance());
      }
      checkGroupElements(expectedHitsA, groups.get(0)
        .getHits());
      checkGroupElements(expectedHitsB, groups.get(1)
        .getHits());
    } finally {
      passageSchema.cleanupWeaviate(syncClient);
    }
  }

  @Test
  public void testGraphQLGetWithGroupByWithHybrid() {
    Field[] hits = new Field[]{ Field.builder()
      .name("content").build(), Field.builder()
      .name("_additional{id distance}").build(),
    };
    Field group = Field.builder()
      .name("group")
      .fields(new Field[]{ Field.builder()
        .name("id").build(), Field.builder()
        .name("groupedBy")
        .fields(new Field[]{ Field.builder()
          .name("value").build(), Field.builder()
          .name("path").build(),
        }).build(), Field.builder()
        .name("count").build(), Field.builder()
        .name("maxDistance").build(), Field.builder()
        .name("minDistance").build(), Field.builder()
        .name("hits")
        .fields(hits).build(),
      })
      .build();
    Field _additional = Field.builder()
      .name("_additional")
      .fields(new Field[]{ group })
      .build();
    Field content = Field.builder()
      .name("content")
      .build(); // Property that we group by
    GroupByArgument groupBy = client.graphQL()
      .arguments()
      .groupByArgBuilder()
      .path(new String[]{ "content" })
      .groups(3)
      .objectsPerGroup(10)
      .build();

    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(new String[]{ "Passage content 2" })
      .build();
    HybridArgument hybrid = HybridArgument.builder()
      .searches(HybridArgument.Searches.builder()
        .nearText(nearText)
        .build())
      .query("Passage content 2")
      .alpha(0.9f)
      .build();

    passageSchema.createAndInsertData(syncClient);

    try {
      Result<GraphQLResponse> groupByResult = doGet(get -> get.withClassName(passageSchema.PASSAGE)
        .withHybrid(hybrid)
        .withGroupBy(groupBy)
        .withFields(content, _additional));

      List<Map<String, Object>> result = extractClass(groupByResult, "Get", passageSchema.PASSAGE);
      Assertions.assertThat(result)
        .isNotNull()
        .hasSize(3);
      List<Group> groups = getGroups(result);
      Assertions.assertThat(groups)
        .isNotNull()
        .hasSize(3);
      for (int i = 0; i < 3; i++) {
        if (i == 0) {
          Assertions.assertThat(groups.get(i).groupedBy.value)
            .isEqualTo("Passage content 2");
        }
        Assertions.assertThat(groups.get(i).minDistance)
          .isEqualTo(groups.get(i)
            .getHits()
            .get(0)
            .get_additional()
            .getDistance());
        Assertions.assertThat(groups.get(i).maxDistance)
          .isEqualTo(groups.get(i)
            .getHits()
            .get(groups.get(i)
              .getHits()
              .size() - 1)
            .get_additional()
            .getDistance());
      }
    } finally {
      passageSchema.cleanupWeaviate(syncClient);
    }
  }

  @Test
  public void shouldSupportSearchByUUID() {
    String className = "ClassUUID";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .description("class with uuid properties")
      .properties(Arrays.asList(
        Property.builder()
          .dataType(Collections.singletonList(DataType.UUID))
          .name("uuidProp")
          .build(), Property.builder()
          .dataType(Collections.singletonList(DataType.UUID_ARRAY))
          .name("uuidArrayProp")
          .build()
      ))
      .build();

    String id = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> properties = new HashMap<>();
    properties.put("uuidProp", "7aaa79d3-a564-45db-8fa8-c49e20b8a39a");
    properties.put("uuidArrayProp", new String[]{ "f70512a3-26cb-4ae4-9369-204555917f15", "9e516f40-fd54-4083-a476-f4675b2b5f92"
    });

    Result<Boolean> createStatus = syncClient.schema()
      .classCreator()
      .withClass(clazz)
      .run();
    Assertions.assertThat(createStatus)
      .isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    Result<WeaviateObject> objectStatus = syncClient.data()
      .creator()
      .withClassName(className)
      .withID(id)
      .withProperties(properties)
      .run();
    Assertions.assertThat(objectStatus)
      .isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult)
      .isNotNull();

    Field fieldId = _additional("id");
    WhereArgument whereUuid = whereText("uuidProp", Operator.Equal, "7aaa79d3-a564-45db-8fa8-c49e20b8a39a");
    WhereArgument whereUuidArray1 = whereText("uuidArrayProp", Operator.Equal, "f70512a3-26cb-4ae4-9369-204555917f15");
    WhereArgument whereUuidArray2 = whereText("uuidArrayProp", Operator.Equal, "9e516f40-fd54-4083-a476-f4675b2b5f92");

    Result<GraphQLResponse> resultUuid = doGet(get -> get.withWhere(whereUuid)
      .withClassName(className)
      .withFields(fieldId));
    Result<GraphQLResponse> resultUuidArray1 = doGet(get -> get.withWhere(whereUuidArray1)
      .withClassName(className)
      .withFields(fieldId));
    Result<GraphQLResponse> resultUuidArray2 = doGet(get -> get.withWhere(whereUuidArray2)
      .withClassName(className)
      .withFields(fieldId));

    assertIds(className, resultUuid, new String[]{ id });
    assertIds(className, resultUuidArray1, new String[]{ id });
    assertIds(className, resultUuidArray2, new String[]{ id });

    Result<Boolean> deleteStatus = syncClient.schema()
      .allDeleter()
      .run();
    Assertions.assertThat(deleteStatus)
      .isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
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

  private SortArgument sort(SortOrder ord, String... properties) {
    return gql.arguments()
      .sortArgBuilder()
      .path(properties)
      .order(ord)
      .build();
  }

  private void assertWhereResultSize(int expectedSize, Result<GraphQLResponse> result, String className) {
    List<?> getClass = extractClass(result, "Get", className);
    assertEquals(expectedSize, getClass.size());
  }


  @SuppressWarnings("unchecked")
  private <T> T extractAdditional(Result<GraphQLResponse> result, String queryType, String className, String fieldName) {
    List<?> objects = extractClass(result, queryType, className);

    Map<String, Map<String, Object>> firstObject = (Map<String, Map<String, Object>>) objects.get(0);
    Map<String, Object> additional = firstObject.get("_additional");

    return (T) additional.get(fieldName);
  }

  private Float[] extractVector(Result<GraphQLResponse> result, String queryType, String className) {
    ArrayList<Double> vector = extractAdditional(result, queryType, className, "vector");
    Float[] out = new Float[vector.size()];
    for (int i = 0; i < vector.size(); i++) {
      out[i] = vector.get(i)
        .floatValue();
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  private void assertAggregateMetaCount(Result<GraphQLResponse> result, String className, int wantObjects, Double wantCount) {
    List<?> objects = extractClass(result, "Aggregate", className);

    assertEquals(wantObjects, objects.size(), "wrong number of objects");
    Map<String, Map<String, Object>> firstObject = (Map<String, Map<String, Object>>) objects.get(0);
    Map<String, Object> meta = firstObject.get("meta");
    assertEquals(wantCount, meta.get("count"), "wrong meta:count");
  }

  private void assertObjectNamesEqual(Result<GraphQLResponse> result, String queryType, String className, String[] want) {
    List<Map<String, String>> objects = extractClass(result, queryType, className);
    assertEquals(want.length, objects.size());
    for (int i = 0; i < want.length; i++) {
      assertEquals(want[i], objects.get(i)
        .get("name"), String.format("%s[%d] has wrong name", className.toLowerCase(), i));
    }
  }
}
