package technology.semi.weaviate.integration.client.graphql;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.filters.Operator;
import technology.semi.weaviate.client.v1.filters.WhereFilter;
import technology.semi.weaviate.client.v1.graphql.model.ExploreFields;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.argument.Bm25Argument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupType;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextMoveParameters;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortOrder;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClientGraphQLTest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
          new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    address = host + ":" + port;
  }

  @Test
  public void testGraphQLGet() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(name).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getPizza = (List) get.get("Pizza");
    assertEquals(4, getPizza.size());
  }


  @Test
  public void testRawGraphQL() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().raw().withQuery("{Get{Pizza{_additional{id}}}}").run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getPizza = (List) get.get("Pizza");
    assertEquals(4, getPizza.size());
}
            
              
  @Test
  public void testGraphQLGetWithNearObjectAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    WeaviateObject soupWithID = WeaviateObject.builder().className("Soup").id(newObjID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustSoup");
      put("description", "soup with id");
    }}).build();
    NearObjectArgument nearObjectArgument = client.graphQL().arguments().nearObjectArgBuilder()
            .id(newObjID).certainty(0.99f).build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("certainty").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(soupWithID).run();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Soup")
            .withNearObject(nearObjectArgument)
            .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(insert);
    assertNotNull(insert.getResult());
    assertEquals(1, insert.getResult().length);
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Soup"));
    assertTrue(get.get("Soup") instanceof List);
    List getSoup = (List) get.get("Soup");
    assertEquals(1, getSoup.size());
  }

  @Test
  public void testGraphQLGetWithNearObjectAndDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    WeaviateObject soupWithID = WeaviateObject.builder().className("Soup").id(newObjID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustSoup");
      put("description", "soup with id");
    }}).build();
    NearObjectArgument nearObjectArgument = client.graphQL().arguments().nearObjectArgBuilder()
            .id(newObjID).distance(0.01f).build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("certainty").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(soupWithID).run();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Soup")
            .withNearObject(nearObjectArgument)
            .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(insert);
    assertNotNull(insert.getResult());
    assertEquals(1, insert.getResult().length);
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Soup"));
    assertTrue(get.get("Soup") instanceof List);
    List getSoup = (List) get.get("Soup");
    assertEquals(1, getSoup.size());
  }


  @Test
  public void testBm25() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
   
    Bm25Argument bm25 = client.graphQL().arguments().bm25ArgBuilder()
            .query("some say revolution")
            .vector(new Float[]{1.0f, 2.0f, 3.0f})
            .alpha(0.8f)
            .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("name").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
            .withBm25(bm25)
            .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getSoup = (List) get.get("Pizza");
    assertEquals(1, getSoup.size());
  }


  @Test
  public void testGraphQLGetWithNearTextAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{"Universally"}).force(0.8f)
            .build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
            .concepts(new String[]{"some say revolution"})
            .moveAwayFrom(moveAway)
            .certainty(0.8f)
            .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("certainty").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
            .withNearText(nearText)
            .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getSoup = (List) get.get("Pizza");
    assertEquals(1, getSoup.size());
  }

  @Test
  public void testGraphQLGetWithNearTextAndDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{"Universally"}).force(0.8f)
            .build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
            .concepts(new String[]{"some say revolution"})
            .moveAwayFrom(moveAway)
            .distance(0.4f)
            .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("certainty").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
            .withNearText(nearText)
            .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getSoup = (List) get.get("Pizza");
    assertEquals(1, getSoup.size());
  }

  @Test
  public void testGraphQLGetWithNearTextAndMoveParamsAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String newObjID1 = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    String newObjID2 = "6baed48e-2afe-4be4-a09d-b00a955d962a";
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID1).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustPizza1");
      put("description", "Universally pizza with id");
    }}).build();
    WeaviateObject pizzaWithID2 = WeaviateObject.builder().className("Pizza").id(newObjID2).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustPizza2");
      put("description", "Universally pizza with some other id");
    }}).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id(newObjID1).build()
      }).force(0.9f).build();
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id(newObjID2).build()
      }).force(0.9f).build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
      .concepts(new String[]{"Universally pizza with id"})
      .moveAwayFrom(moveAway)
      .moveTo(moveTo)
      .certainty(0.4f)
      .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
      .name("_additional")
      .fields(new Field[]{Field.builder().name("certainty").build()})
      .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID, pizzaWithID2).run();
    Result<GraphQLResponse> result = client.graphQL().get()
      .withClassName("Pizza")
      .withNearText(nearText)
      .withFields(name, _additional)
      .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(insert);
    assertNotNull(insert.getResult());
    assertEquals(2, insert.getResult().length);
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List pizzas = (List) get.get("Pizza");
    assertEquals(6, pizzas.size());
  }

  @Test
  public void testGraphQLGetWithNearTextAndMoveParamsAndDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String newObjID1 = "6baed48e-2afe-4be4-a09d-b00a955d962b";
    String newObjID2 = "6baed48e-2afe-4be4-a09d-b00a955d962a";
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID1).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustPizza1");
      put("description", "Universally pizza with id");
    }}).build();
    WeaviateObject pizzaWithID2 = WeaviateObject.builder().className("Pizza").id(newObjID2).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustPizza2");
      put("description", "Universally pizza with some other id");
    }}).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id(newObjID1).build()
            }).force(0.9f).build();
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id(newObjID2).build()
            }).force(0.9f).build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
            .concepts(new String[]{"Universally pizza with id"})
            .moveAwayFrom(moveAway)
            .moveTo(moveTo)
            .distance(0.6f)
            .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("certainty").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID, pizzaWithID2).run();
    Result<GraphQLResponse> result = client.graphQL().get()
            .withClassName("Pizza")
            .withNearText(nearText)
            .withFields(name, _additional)
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(insert);
    assertNotNull(insert.getResult());
    assertEquals(2, insert.getResult().length);
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List pizzas = (List) get.get("Pizza");
    assertEquals(6, pizzas.size());
  }

  @Test
  public void testGraphQLGetWithNearTextAndLimitAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
            .concepts(new String[]{"some say revolution"})
            .certainty(0.8f)
            .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("certainty").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
            .withNearText(nearText)
            .withLimit(1)
            .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getSoup = (List) get.get("Pizza");
    assertEquals(1, getSoup.size());
  }

  @Test
  public void testGraphQLGetWithNearTextAndLimitAndDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
            .concepts(new String[]{"some say revolution"})
            .distance(0.4f)
            .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("certainty").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
            .withNearText(nearText)
            .withLimit(1)
            .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getSoup = (List) get.get("Pizza");
    assertEquals(1, getSoup.size());
  }

  @Test
  public void testGraphQLGetWithWhereByFieldTokenizedProperty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();

    WhereFilter whereFullString = WhereFilter.builder()
            .path(new String[]{ "name" })
            .operator(Operator.Equal)
            .valueString("Frutti di Mare")
            .build();
    WhereFilter wherePartString = WhereFilter.builder()
            .path(new String[]{ "name" })
            .operator(Operator.Equal)
            .valueString("Frutti")
            .build();
    WhereFilter whereFullText = WhereFilter.builder()
            .path(new String[]{ "description" })
            .operator(Operator.Equal)
            .valueText("Universally accepted to be the best pizza ever created.")
            .build();
    WhereFilter wherePartText = WhereFilter.builder()
            .path(new String[]{ "description" })
            .operator(Operator.Equal)
            .valueText("Universally")
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> resultFullString = client.graphQL().get().withWhere(whereFullString).withClassName("Pizza").withFields(name).run();
    Result<GraphQLResponse> resultPartString = client.graphQL().get().withWhere(wherePartString).withClassName("Pizza").withFields(name).run();
    Result<GraphQLResponse> resultFullText = client.graphQL().get().withWhere(whereFullText).withClassName("Pizza").withFields(name).run();
    Result<GraphQLResponse> resultPartText = client.graphQL().get().withWhere(wherePartText).withClassName("Pizza").withFields(name).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertWhereResultSize(1, resultFullString, "Pizza");
    assertWhereResultSize(0, resultPartString, "Pizza");
    assertWhereResultSize(1, resultFullText, "Pizza");
    assertWhereResultSize(1, resultPartText, "Pizza");
  }

  @Test
  public void testGraphQLGetWithWhereByDate() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();

    // 2022-02-01T00:00:00+0100
    Date date = new Date(2022-1900, 2-1, 1, 0, 0, 0);

    WhereFilter whereDate = WhereFilter.builder()
            .path(new String[]{ "bestBefore" })
            .operator(Operator.GreaterThan)
            .valueDate(date)
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> resultDate = client.graphQL().get().withWhere(whereDate).withClassName("Pizza").withFields(name).run();
    testGenerics.cleanupWeaviate(client);
    // then
    List<Map<String, Object>> maps = extractResult(resultDate, "Pizza");
    assertThat(maps).hasSize(3);
    assertThat(maps).extracting(el -> (String) el.get("name")).contains("Frutti di Mare", "Hawaii", "Doener");
  }

  @Test
  public void testGraphQLExploreWithCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    String[] concepts = new String[]{"pineapple slices", "ham"};
    NearTextMoveParameters moveTo = client.graphQL().arguments().nearTextMoveParameterBuilder()
            .concepts(new String[]{"Pizza"}).force(0.3f).build();
    NearTextMoveParameters moveAwayFrom = client.graphQL().arguments().nearTextMoveParameterBuilder()
            .concepts(new String[]{"toast", "bread"}).force(0.4f).build();
    NearTextArgument withNearText = client.graphQL().arguments().nearTextArgBuilder()
            .concepts(concepts).certainty(0.40f)
            .moveTo(moveTo).moveAwayFrom(moveAwayFrom)
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().explore().withFields(fields).withNearText(withNearText).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNull(resp.getErrors());
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Explore"));
    assertTrue(data.get("Explore") instanceof List);
    List get = (List) data.get("Explore");
    assertEquals(6, get.size());
  }

  @Test
  public void testGraphQLExploreWithDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    String[] concepts = new String[]{"pineapple slices", "ham"};
    NearTextMoveParameters moveTo = client.graphQL().arguments().nearTextMoveParameterBuilder()
            .concepts(new String[]{"Pizza"}).force(0.3f).build();
    NearTextMoveParameters moveAwayFrom = client.graphQL().arguments().nearTextMoveParameterBuilder()
            .concepts(new String[]{"toast", "bread"}).force(0.4f).build();
    NearTextArgument withNearText = client.graphQL().arguments().nearTextArgBuilder()
            .concepts(concepts).distance(0.80f)
            .moveTo(moveTo).moveAwayFrom(moveAwayFrom)
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().explore().withFields(fields).withNearText(withNearText).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNull(resp.getErrors());
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Explore"));
    assertTrue(data.get("Explore") instanceof List);
    List get = (List) data.get("Explore");
    assertEquals(6, get.size());
  }

  @Test
  public void testGraphQLAggregate() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithWhereFilter() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d96ee";
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustPizza");
      put("description", "pizza with id");
    }}).build();
    WhereFilter where = WhereFilter.builder()
            .path(new String[]{ "id" })
            .operator(Operator.Equal)
            .valueString(newObjID)
            .build();
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withWhere(where).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(insert);
    assertNotNull(insert.getResult());
    assertEquals(1, insert.getResult().length);
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 1.0d);
  }

  @Test
  public void testGraphQLAggregateWithGroupedByAndWhere() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d96ee";
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustPizza");
      put("description", "pizza with id");
    }}).build();
    WhereFilter where = WhereFilter.builder()
            .path(new String[]{ "id" })
            .operator(Operator.Equal)
            .valueString(newObjID)
            .build();
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withGroupBy("name").withWhere(where).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(insert);
    assertNotNull(insert.getResult());
    assertEquals(1, insert.getResult().length);
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 1.0d);
  }

  @Test
  public void testGraphQLAggregateWithGroupedBy() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String newObjID = "6baed48e-2afe-4be4-a09d-b00a955d96ee";
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "JustPizza");
      put("description", "pizza with id");
    }}).build();
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withGroupBy("name").run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(insert);
    assertNotNull(insert.getResult());
    assertEquals(1, insert.getResult().length);
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 5, 1.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearVector() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);
    Field additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("vector").build()})
            .build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = result.getResult();
    Float[] vec = getVectorFromResponse(resp);

    // when
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    NearVectorArgument nearVector = NearVectorArgument.builder().certainty(0.7f).vector(vec).build();
    result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withNearVector(nearVector).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertFalse(result.hasErrors());
    resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearObjectAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);
    Field additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("id").build()})
            .build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = result.getResult();
    String id = getAdditionalFieldFromResponse(resp, "id");

    // when
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    NearObjectArgument nearObject = NearObjectArgument.builder().certainty(0.7f).id(id).build();
    result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withNearObject(nearObject).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertFalse(result.hasErrors());
    resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearObjectAndDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);
    Field additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("id").build()})
            .build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = result.getResult();
    String id = getAdditionalFieldFromResponse(resp, "id");

    // when
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    NearObjectArgument nearObject = NearObjectArgument.builder().distance(0.3f).id(id).build();
    result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withNearObject(nearObject).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertFalse(result.hasErrors());
    resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearTextAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    // when
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    NearTextArgument nearText = NearTextArgument.builder().certainty(0.7f).concepts(new String[]{"pizza"}).build();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withNearText(nearText).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearTextAndDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    // when
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    NearTextArgument nearText = NearTextArgument.builder().distance(0.6f).concepts(new String[]{"pizza"}).build();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza").withNearText(nearText).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithObjectLimitAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    // when
    Integer objectLimit = 1;
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    NearTextArgument nearText = NearTextArgument.builder().certainty(0.7f).concepts(new String[]{"pizza"}).build();
    Result<GraphQLResponse> result = client.graphQL()
            .aggregate()
            .withFields(meta)
            .withClassName("Pizza")
            .withNearText(nearText)
            .withObjectLimit(objectLimit)
            .run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, Double.valueOf(objectLimit));
  }

  @Test
  public void testGraphQLAggregateWithObjectLimitAndDistance() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    // when
    Integer objectLimit = 1;
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    NearTextArgument nearText = NearTextArgument.builder().distance(0.3f).concepts(new String[]{"pizza"}).build();
    Result<GraphQLResponse> result = client.graphQL()
            .aggregate()
            .withFields(meta)
            .withClassName("Pizza")
            .withNearText(nearText)
            .withObjectLimit(objectLimit)
            .run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, Double.valueOf(objectLimit));
  }

  @Test
  public void testGraphQLGetWithGroup() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    GroupArgument group = client.graphQL().arguments().groupArgBuilder()
            .type(GroupType.merge).force(1.0f).build();
    Field name = Field.builder().name("name").build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
            .withClassName("Soup")
            .withFields(name)
            .withGroup(group)
            .withLimit(7)
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Soup"));
    assertTrue(get.get("Soup") instanceof List);
    List getSoup = (List) get.get("Soup");
    assertEquals(1, getSoup.size());
  }

  @Test
  public void testGraphQLGetWithSort() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    SortArgument byNameDesc = client.graphQL().arguments().sortArgBuilder()
            .path(new String[]{ "name" })
            .order(SortOrder.desc)
            .build();
    String[] expectedByNameDesc = new String[]{"Quattro Formaggi", "Hawaii", "Frutti di Mare", "Doener"};
    SortArgument byPriceAsc = client.graphQL().arguments().sortArgBuilder()
            .path(new String[]{ "price" })
            .order(SortOrder.asc)
            .build();
    String[] expectedByPriceAsc = new String[]{ "Hawaii", "Doener", "Quattro Formaggi", "Frutti di Mare" };
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> resultByNameDesc = client.graphQL().get()
            .withClassName("Pizza")
            .withSort(byNameDesc)
            .withFields(name).run();
    Result<GraphQLResponse> resultByDescriptionAsc = client.graphQL().get()
            .withClassName("Pizza")
            .withSort(byPriceAsc)
            .withFields(name).run();
    Result<GraphQLResponse> resultByNameDescByPriceAsc = client.graphQL().get()
            .withClassName("Pizza")
            .withSort(byNameDesc, byPriceAsc)
            .withFields(name).run();
    testGenerics.cleanupWeaviate(client);
    // then
    expectPizzaNamesOrder(resultByNameDesc, expectedByNameDesc);
    expectPizzaNamesOrder(resultByDescriptionAsc, expectedByPriceAsc);
    expectPizzaNamesOrder(resultByNameDescByPriceAsc, expectedByNameDesc);
  }

  @Test
  public void testGraphQLGetWithTimestampFilters() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);
    Field additional = Field.builder()
        .name("_additional")
        .fields(new Field[]{
            Field.builder().name("id").build(),
            Field.builder().name("creationTimeUnix").build(),
            Field.builder().name("lastUpdateTimeUnix").build()
        })
        .build();
    Result<GraphQLResponse> expected = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = expected.getResult();
    String expectedCreateTime = getAdditionalFieldFromResponse(resp, "creationTimeUnix");
    String expectedUpdateTime = getAdditionalFieldFromResponse(resp, "lastUpdateTimeUnix");
    WhereFilter createTimeFilter = WhereFilter.builder()
            .path(new String[]{ "_creationTimeUnix" })
            .operator(Operator.Equal)
            .valueString(expectedCreateTime)
            .build();
    WhereFilter updateTimeFilter = WhereFilter.builder()
            .path(new String[]{ "_lastUpdateTimeUnix" })
            .operator(Operator.Equal)
            .valueString(expectedCreateTime)
            .build();
    // when
    Result<GraphQLResponse> createTimeResult = client.graphQL().get()
        .withClassName("Pizza")
        .withWhere(createTimeFilter)
        .withFields(additional).run();
    Result<GraphQLResponse> updateTimeResult = client.graphQL().get()
        .withClassName("Pizza")
        .withWhere(updateTimeFilter)
        .withFields(additional).run();
    // then
    String resultCreateTime = getAdditionalFieldFromResponse(createTimeResult.getResult(), "creationTimeUnix");
    assertEquals(expectedCreateTime, resultCreateTime);
    String resultUpdateTime = getAdditionalFieldFromResponse(updateTimeResult.getResult(), "lastUpdateTimeUnix");
    assertEquals(expectedUpdateTime, resultUpdateTime);
    testGenerics.cleanupWeaviate(client);
  }

  private void expectPizzaNamesOrder(Result<GraphQLResponse> result, String[] expectedPizzas) {
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List pizzas = (List) get.get("Pizza");
    assertEquals(expectedPizzas.length, pizzas.size());
    for (int i=0; i<pizzas.size(); i++) {
      assertPizzaName(expectedPizzas[i], pizzas, i);
    }
  }

  private void assertPizzaName(String name, List pizzas, int position) {
    assertTrue(pizzas.get(position) instanceof Map);
    Map pizza = (Map)  pizzas.get(position);
    assertNotNull(pizza.get("name"));
    assertEquals(name, pizza.get("name"));
  }

  private void checkAggregateMetaCount(GraphQLResponse resp, int expectedResultSize, Double expectedCount) {
    assertNotNull(resp);
    assertNull(resp.getErrors());
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Aggregate"));
    assertTrue(data.get("Aggregate") instanceof Map);
    Map aggregate = (Map) data.get("Aggregate");
    assertNotNull(aggregate.get("Pizza"));
    assertTrue(aggregate.get("Pizza") instanceof List);
    List res = (List) aggregate.get("Pizza");
    assertEquals(expectedResultSize, res.size());
    assertTrue(res.get(0) instanceof Map);
    Map count = (Map) res.get(0);
    assertNotNull(count.get("meta"));
    assertTrue(count.get("meta") instanceof Map);
    Map countVal = (Map) count.get("meta");
    assertEquals(expectedCount, countVal.get("count"));
  }

  private List<Map<String, Object>> extractResult(Result<GraphQLResponse> result, String className) {
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get(className));
    assertTrue(get.get(className) instanceof List);
    return (List) get.get(className);
  }

  private void assertWhereResultSize(int expectedSize, Result<GraphQLResponse> result, String className) {
    List getClass = extractResult(result, className);
    assertEquals(expectedSize, getClass.size());
  }

  private Float[] getVectorFromResponse(GraphQLResponse resp) {
    assertNotNull(resp);
    assertNull(resp.getErrors());
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List pizza = (List) get.get("Pizza");
    assertTrue(pizza.get(0) instanceof Map);
    Map firstPizza = (Map) pizza.get(0);
    Map additional = (Map) firstPizza.get("_additional");

    ArrayList vec = (ArrayList) additional.get("vector");
    Float[] res = new Float[vec.size()];
    for (int i = 0; i < vec.size(); i++) {
      res[i] = ((Double) vec.get(i)).floatValue();
    }

    return res;
  }

  private String getAdditionalFieldFromResponse(GraphQLResponse resp, String fieldName) {
    assertNotNull(resp);
    assertNull(resp.getErrors());
    assertNotNull(resp.getData());
    assertTrue(resp.getData() instanceof Map);
    Map data = (Map) resp.getData();
    assertNotNull(data.get("Get"));
    assertTrue(data.get("Get") instanceof Map);
    Map get = (Map) data.get("Get");
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List pizza = (List) get.get("Pizza");
    assertTrue(pizza.get(0) instanceof Map);
    Map firstPizza = (Map) pizza.get(0);
    Map additional = (Map) firstPizza.get("_additional");
    String targetField = (String) additional.get(fieldName);
    return targetField;
  }
}
