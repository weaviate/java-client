package technology.semi.weaviate.integration.client.graphql;

import java.io.File;
import java.util.*;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.lang.ArrayUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.graphql.model.ExploreFields;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.argument.*;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

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
    Fields fields = Fields.builder().fields(new Field[]{name}).build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(fields).run();
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
  public void testGraphQLGetWithNearObject() {
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
    Fields fields = Fields.builder()
            .fields(new Field[]{name, _additional})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObject(soupWithID).run();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Soup")
            .withNearObject(nearObjectArgument)
            .withFields(fields).run();
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
  public void testGraphQLGetWithNearText() {
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
    Fields fields = Fields.builder()
            .fields(new Field[]{name, _additional})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
            .withNearText(nearText)
            .withFields(fields).run();
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
  public void testGraphQLGetWithNearTextAndLimit() {
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
    Fields fields = Fields.builder()
            .fields(new Field[]{name, _additional})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
            .withNearText(nearText)
            .withLimit(1)
            .withFields(fields).run();
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
    Fields fields = Fields.builder().fields(new Field[]{name}).build();

    WhereArgument whereFullString = WhereArgument.builder()
            .path(new String[]{ "name" })
            .operator(WhereOperator.Equal)
            .valueString("Frutti di Mare")
            .build();
    WhereArgument wherePartString = WhereArgument.builder()
            .path(new String[]{ "name" })
            .operator(WhereOperator.Equal)
            .valueString("Frutti")
            .build();
    WhereArgument whereFullText = WhereArgument.builder()
            .path(new String[]{ "description" })
            .operator(WhereOperator.Equal)
            .valueText("Universally accepted to be the best pizza ever created.")
            .build();
    WhereArgument wherePartText = WhereArgument.builder()
            .path(new String[]{ "description" })
            .operator(WhereOperator.Equal)
            .valueText("Universally")
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> resultFullString = client.graphQL().get().withWhere(whereFullString).withClassName("Pizza").withFields(fields).run();
    Result<GraphQLResponse> resultPartString = client.graphQL().get().withWhere(wherePartString).withClassName("Pizza").withFields(fields).run();
    Result<GraphQLResponse> resultFullText = client.graphQL().get().withWhere(whereFullText).withClassName("Pizza").withFields(fields).run();
    Result<GraphQLResponse> resultPartText = client.graphQL().get().withWhere(wherePartText).withClassName("Pizza").withFields(fields).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertWhereResultSize(1, resultFullString, "Pizza");
    assertWhereResultSize(0, resultPartString, "Pizza");
    assertWhereResultSize(1, resultFullText, "Pizza");
    assertWhereResultSize(1, resultPartText, "Pizza");
  }

  @Test
  public void testGraphQLExplore() {
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
            .concepts(concepts).certainty(0.71f)
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
    assertEquals(5, get.size());
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
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(fields).withClassName("Pizza").run();
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
    WhereArgument where = WhereArgument.builder()
            .path(new String[]{ "id" })
            .operator(WhereOperator.Equal)
            .valueString(newObjID)
            .build();
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObject(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(fields).withClassName("Pizza").withWhere(where).run();
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
    WhereArgument where = WhereArgument.builder()
            .path(new String[]{ "id" })
            .operator(WhereOperator.Equal)
            .valueString(newObjID)
            .build();
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObject(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(fields).withClassName("Pizza").withGroupBy("name").withWhere(where).run();
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
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObject(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(fields).withClassName("Pizza").withGroupBy("name").run();
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
    Fields fields = Fields.builder().fields(new Field[]{additional}).build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(fields).run();
    GraphQLResponse resp = result.getResult();
    Float[] vec = getVectorFromResponse(resp);

    // when
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    fields = Fields.builder().fields(new Field[]{meta}).build();
    NearVectorArgument nearVector = NearVectorArgument.builder().certainty(0.7f).vector(vec).build();
    result = client.graphQL().aggregate().withFields(fields).withClassName("Pizza").withNearVector(nearVector).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertNotNull(result);
    assertFalse(result.hasErrors());
    resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearObject() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);
    Field additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{Field.builder().name("id").build()})
            .build();
    Fields fields = Fields.builder().fields(new Field[]{additional}).build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(fields).run();
    GraphQLResponse resp = result.getResult();
    String id = getIdFromResponse(resp);

    // when
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{Field.builder().name("count").build()})
            .build();
    fields = Fields.builder().fields(new Field[]{meta}).build();
    NearObjectArgument nearObject = NearObjectArgument.builder().certainty(0.7f).id(id).build();
    result = client.graphQL().aggregate().withFields(fields).withClassName("Pizza").withNearObject(nearObject).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertNotNull(result);
    assertFalse(result.hasErrors());
    resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
  }

  @Test
  public void testGraphQLAggregateWithNearText() {
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
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
    NearTextArgument nearText = NearTextArgument.builder().certainty(0.7f).concepts(new String[]{"pizza"}).build();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(fields).withClassName("Pizza").withNearText(nearText).run();
    testGenerics.cleanupWeaviate(client);

    // then
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLResponse resp = result.getResult();
    checkAggregateMetaCount(resp, 1, 4.0d);
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
    Fields fields = Fields.builder()
            .fields(new Field[]{name})
            .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
            .withClassName("Soup")
            .withFields(fields)
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

  private void assertWhereResultSize(int expectedSize, Result<GraphQLResponse> result, String className) {
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
    List getClass = (List) get.get(className);
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

  private String getIdFromResponse(GraphQLResponse resp) {
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
    String id = (String) additional.get("id");
    return id;
  }
}
