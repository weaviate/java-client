package io.weaviate.integration.client.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponseStatus;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.ExploreFields;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.Bm25Argument;
import io.weaviate.client.v1.graphql.query.argument.Bm25Argument.SearchOperator;
import io.weaviate.client.v1.graphql.query.argument.GroupArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupByArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupType;
import io.weaviate.client.v1.graphql.query.argument.HybridArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextMoveParameters;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArgument;
import io.weaviate.client.v1.graphql.query.argument.SortOrder;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.GenerativeSearchBuilder;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;

public class ClientGraphQLTest extends AbstractClientGraphQLTest {
  private String address;
  private String openAIApiKey;
  private static final WeaviateTestGenerics.DocumentPassageSchema testData = new WeaviateTestGenerics.DocumentPassageSchema();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();
    openAIApiKey = System.getenv("OPENAI_APIKEY");
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
    WeaviateObject soupWithID = WeaviateObject.builder().className("Soup").id(newObjID)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustSoup");
            put("description", "soup with id");
          }
        }).build();
    NearObjectArgument nearObjectArgument = client.graphQL().arguments().nearObjectArgBuilder()
        .id(newObjID).certainty(0.99f).build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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
    WeaviateObject soupWithID = WeaviateObject.builder().className("Soup").id(newObjID)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustSoup");
            put("description", "soup with id");
          }
        }).build();
    NearObjectArgument nearObjectArgument = client.graphQL().arguments().nearObjectArgBuilder()
        .id(newObjID).distance(0.01f).build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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
        .query("innovation")
        .properties(new String[] { "description" })
        .build();
    Field name = Field.builder().name("description").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("id").build() })
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
    List pizza = (List) get.get("Pizza");
    assertEquals(1, pizza.size());
    Map fields = (Map) pizza.get(0);
    String descr = (String) fields.get("description");
    assertTrue(descr.contains("innovation"));
  }

  @Test
  public void testBm25_searchOperator_And() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

    Bm25Argument bm25 = client.graphQL().arguments().bm25ArgBuilder()
        .query("innovation")
        .properties(new String[] { "description" })
        .searchOperator(SearchOperator.and())
        .build();

    Field name = Field.builder().name("description").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("id").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
        .withBm25(bm25)
        .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertNull(result.getError());
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
    List pizza = (List) get.get("Pizza");
    assertEquals(1, pizza.size());
    Map fields = (Map) pizza.get(0);
    String descr = (String) fields.get("description");
    assertTrue(descr.contains("innovation"));
  }

  @Test
  public void testBm25_searchOperator_Or() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

    Bm25Argument bm25 = client.graphQL().arguments().bm25ArgBuilder()
        .query("innovation")
        .properties(new String[] { "description" })
        .searchOperator(SearchOperator.or(1))
        .build();

    Field name = Field.builder().name("description").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("id").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
        .withBm25(bm25)
        .withFields(name, _additional).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(result);
    assertNull(result.getError());
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
    List pizza = (List) get.get("Pizza");
    assertEquals(1, pizza.size());
    Map fields = (Map) pizza.get(0);
    String descr = (String) fields.get("description");
    assertTrue(descr.contains("innovation"));
  }

  @Test
  public void testHybrid() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

    HybridArgument hybrid = client.graphQL().arguments().hybridArgBuilder()
        .query("some say revolution")
        .alpha(0.8f)
        .build();
    Field name = Field.builder().name("description").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("id").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
        .withHybrid(hybrid)
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
  }

  @Test
  public void testHybrid_bm25SearchOperator_And() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

    HybridArgument hybrid = client.graphQL().arguments().hybridArgBuilder()
        .query("some say revolution")
        .alpha(0.8f)
        .bm25SearchOperator(SearchOperator.and())
        .build();
    Field name = Field.builder().name("description").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("id").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
        .withHybrid(hybrid)
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
  }

  @Test
  public void testHybrid_bm25SearchOperator_Or() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

    HybridArgument hybrid = client.graphQL().arguments().hybridArgBuilder()
        .query("some say revolution")
        .alpha(0.8f)
        .bm25SearchOperator(SearchOperator.or(1))
        .build();
    Field name = Field.builder().name("description").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("id").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza")
        .withHybrid(hybrid)
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
  }

  @Test
  public void testGraphQLGetWithNearTextAndCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
        .concepts(new String[] { "Universally" }).force(0.8f)
        .build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
        .concepts(new String[] { "some say revolution" })
        .moveAwayFrom(moveAway)
        .certainty(0.8f)
        .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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
        .concepts(new String[] { "Universally" }).force(0.8f)
        .build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
        .concepts(new String[] { "some say revolution" })
        .moveAwayFrom(moveAway)
        .distance(0.4f)
        .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID1)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustPizza1");
            put("description", "Universally pizza with id");
          }
        }).build();
    WeaviateObject pizzaWithID2 = WeaviateObject.builder().className("Pizza").id(newObjID2)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustPizza2");
            put("description", "Universally pizza with some other id");
          }
        }).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
        .objects(new NearTextMoveParameters.ObjectMove[] {
            NearTextMoveParameters.ObjectMove.builder().id(newObjID1).build()
        }).force(0.9f).build();
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
        .objects(new NearTextMoveParameters.ObjectMove[] {
            NearTextMoveParameters.ObjectMove.builder().id(newObjID2).build()
        }).force(0.9f).build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
        .concepts(new String[] { "Universally pizza with id" })
        .moveAwayFrom(moveAway)
        .moveTo(moveTo)
        .certainty(0.4f)
        .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID1)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustPizza1");
            put("description", "Universally pizza with id");
          }
        }).build();
    WeaviateObject pizzaWithID2 = WeaviateObject.builder().className("Pizza").id(newObjID2)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustPizza2");
            put("description", "Universally pizza with some other id");
          }
        }).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
        .objects(new NearTextMoveParameters.ObjectMove[] {
            NearTextMoveParameters.ObjectMove.builder().id(newObjID1).build()
        }).force(0.9f).build();
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
        .objects(new NearTextMoveParameters.ObjectMove[] {
            NearTextMoveParameters.ObjectMove.builder().id(newObjID2).build()
        }).force(0.9f).build();
    NearTextArgument nearText = client.graphQL().arguments().nearTextArgBuilder()
        .concepts(new String[] { "Universally pizza with id" })
        .moveAwayFrom(moveAway)
        .moveTo(moveTo)
        .distance(0.6f)
        .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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
        .concepts(new String[] { "some say revolution" })
        .certainty(0.8f)
        .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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
        .concepts(new String[] { "some say revolution" })
        .distance(0.4f)
        .build();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
        .name("_additional")
        .fields(new Field[] { Field.builder().name("certainty").build() })
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

    WhereArgument whereFullString = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "name" })
            .operator(Operator.Equal)
            .valueText("Frutti di Mare")
            .build())
        .build();
    WhereArgument wherePartString = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "name" })
            .operator(Operator.Equal)
            .valueText("Frutti")
            .build())
        .build();
    WhereArgument whereFullText = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "description" })
            .operator(Operator.Equal)
            .valueText("Universally accepted to be the best pizza ever created.")
            .build())
        .build();
    WhereArgument wherePartText = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "description" })
            .operator(Operator.Equal)
            .valueText("Universally")
            .build())
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> resultFullString = client.graphQL().get().withWhere(whereFullString).withClassName("Pizza")
        .withFields(name).run();
    Result<GraphQLResponse> resultPartString = client.graphQL().get().withWhere(wherePartString).withClassName("Pizza")
        .withFields(name).run();
    Result<GraphQLResponse> resultFullText = client.graphQL().get().withWhere(whereFullText).withClassName("Pizza")
        .withFields(name).run();
    Result<GraphQLResponse> resultPartText = client.graphQL().get().withWhere(wherePartText).withClassName("Pizza")
        .withFields(name).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertWhereResultSize(1, resultFullString, "Pizza");
    assertWhereResultSize(0, resultPartString, "Pizza");
    assertWhereResultSize(1, resultFullText, "Pizza");
    assertWhereResultSize(1, resultPartText, "Pizza");
  }

  @Test
  public void shouldSupportDeprecatedValueString() {
    WeaviateClient client = new WeaviateClient(new Config("http", address));
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

    WhereArgument whereString = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "name" })
            .operator(Operator.Equal)
            .valueString("Frutti di Mare")
            .build())
        .build();

    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
        .withWhere(whereString)
        .withClassName("Pizza")
        .withFields(Field.builder().name("name").build())
        .run();
    testGenerics.cleanupWeaviate(client);

    assertWhereResultSize(1, result, "Pizza");
  }

  @Test
  public void testGraphQLGetWithWhereByDate() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();

    Calendar cal = Calendar.getInstance();
    cal.set(2022, Calendar.FEBRUARY, 1, 0, 0, 0);

    WhereArgument whereDate = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "bestBefore" })
            .operator(Operator.GreaterThan)
            .valueDate(cal.getTime())
            .build())
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> resultDate = client.graphQL().get().withWhere(whereDate).withClassName("Pizza")
        .withFields(name).run();
    testGenerics.cleanupWeaviate(client);
    // then
    List<Map<String, Object>> maps = extractResult(resultDate, "Pizza");
    assertThat(maps).hasSize(3)
        .extracting(el -> (String) el.get("name"))
        .contains("Frutti di Mare", "Hawaii", "Doener");
  }

  @Test
  public void testGraphQLExploreWithCertainty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    ExploreFields[] fields = new ExploreFields[] { ExploreFields.CERTAINTY, ExploreFields.BEACON,
        ExploreFields.CLASS_NAME };
    String[] concepts = new String[] { "pineapple slices", "ham" };
    NearTextMoveParameters moveTo = client.graphQL().arguments().nearTextMoveParameterBuilder()
        .concepts(new String[] { "Pizza" }).force(0.3f).build();
    NearTextMoveParameters moveAwayFrom = client.graphQL().arguments().nearTextMoveParameterBuilder()
        .concepts(new String[] { "toast", "bread" }).force(0.4f).build();
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
    ExploreFields[] fields = new ExploreFields[] { ExploreFields.CERTAINTY, ExploreFields.BEACON,
        ExploreFields.CLASS_NAME };
    String[] concepts = new String[] { "pineapple slices", "ham" };
    NearTextMoveParameters moveTo = client.graphQL().arguments().nearTextMoveParameterBuilder()
        .concepts(new String[] { "Pizza" }).force(0.3f).build();
    NearTextMoveParameters moveAwayFrom = client.graphQL().arguments().nearTextMoveParameterBuilder()
        .concepts(new String[] { "toast", "bread" }).force(0.4f).build();
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
        .fields(new Field[] { Field.builder().name("count").build() })
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
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustPizza");
            put("description", "pizza with id");
          }
        }).build();
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "id" })
            .operator(Operator.Equal)
            .valueText(newObjID)
            .build())
        .build();
    Field meta = Field.builder()
        .name("meta")
        .fields(new Field[] { Field.builder().name("count").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza")
        .withWhere(where).run();
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
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustPizza");
            put("description", "pizza with id");
          }
        }).build();
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "id" })
            .operator(Operator.Equal)
            .valueText(newObjID)
            .build())
        .build();
    Field meta = Field.builder()
        .name("meta")
        .fields(new Field[] { Field.builder().name("count").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza")
        .withGroupBy("name").withWhere(where).run();
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
    WeaviateObject pizzaWithID = WeaviateObject.builder().className("Pizza").id(newObjID)
        .properties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "JustPizza");
            put("description", "pizza with id");
          }
        }).build();
    Field meta = Field.builder()
        .name("meta")
        .fields(new Field[] { Field.builder().name("count").build() })
        .build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<ObjectGetResponse[]> insert = client.batch().objectsBatcher().withObjects(pizzaWithID).run();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza")
        .withGroupBy("name").run();
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
        .fields(new Field[] { Field.builder().name("vector").build() })
        .build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = result.getResult();
    Float[] vec = getVectorFromResponse(resp);

    // when
    Field meta = Field.builder()
        .name("meta")
        .fields(new Field[] { Field.builder().name("count").build() })
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
        .fields(new Field[] { Field.builder().name("id").build() })
        .build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = result.getResult();
    String id = getAdditionalFieldFromResponse(resp, "id");

    // when
    Field meta = Field.builder()
        .name("meta")
        .fields(new Field[] { Field.builder().name("count").build() })
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
        .fields(new Field[] { Field.builder().name("id").build() })
        .build();
    Result<GraphQLResponse> result = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = result.getResult();
    String id = getAdditionalFieldFromResponse(resp, "id");

    // when
    Field meta = Field.builder()
        .name("meta")
        .fields(new Field[] { Field.builder().name("count").build() })
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
        .fields(new Field[] { Field.builder().name("count").build() })
        .build();
    NearTextArgument nearText = NearTextArgument.builder().certainty(0.7f).concepts(new String[] { "pizza" }).build();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza")
        .withNearText(nearText).run();
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
        .fields(new Field[] { Field.builder().name("count").build() })
        .build();
    NearTextArgument nearText = NearTextArgument.builder().distance(0.6f).concepts(new String[] { "pizza" }).build();
    Result<GraphQLResponse> result = client.graphQL().aggregate().withFields(meta).withClassName("Pizza")
        .withNearText(nearText).run();
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
        .fields(new Field[] { Field.builder().name("count").build() })
        .build();
    NearTextArgument nearText = NearTextArgument.builder().certainty(0.7f).concepts(new String[] { "pizza" }).build();
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
        .fields(new Field[] { Field.builder().name("count").build() })
        .build();
    NearTextArgument nearText = NearTextArgument.builder().distance(0.3f).concepts(new String[] { "pizza" }).build();
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
        .path(new String[] { "name" })
        .order(SortOrder.desc)
        .build();
    String[] expectedByNameDesc = new String[] { "Quattro Formaggi", "Hawaii", "Frutti di Mare", "Doener" };
    SortArgument byPriceAsc = client.graphQL().arguments().sortArgBuilder()
        .path(new String[] { "price" })
        .order(SortOrder.asc)
        .build();
    String[] expectedByPriceAsc = new String[] { "Hawaii", "Doener", "Quattro Formaggi", "Frutti di Mare" };
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
        .fields(new Field[] {
            Field.builder().name("id").build(),
            Field.builder().name("creationTimeUnix").build(),
            Field.builder().name("lastUpdateTimeUnix").build()
        })
        .build();
    Result<GraphQLResponse> expected = client.graphQL().get().withClassName("Pizza").withFields(additional).run();
    GraphQLResponse resp = expected.getResult();
    String expectedCreateTime = getAdditionalFieldFromResponse(resp, "creationTimeUnix");
    String expectedUpdateTime = getAdditionalFieldFromResponse(resp, "lastUpdateTimeUnix");
    WhereArgument createTimeFilter = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "_creationTimeUnix" })
            .operator(Operator.Equal)
            .valueText(expectedCreateTime)
            .build())
        .build();
    WhereArgument updateTimeFilter = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "_lastUpdateTimeUnix" })
            .operator(Operator.Equal)
            .valueText(expectedCreateTime)
            .build())
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

  @Test
  public void testGraphQLGetUsingCursorAPI() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
        .withClassName("Pizza").withAfter("00000000-0000-0000-0000-000000000000").withLimit(10).withFields(name)
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
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getPizza = (List) get.get("Pizza");
    assertEquals(3, getPizza.size());
  }

  @Test
  public void testGraphQLGetUsingLimitAndOffset() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    // when
    testGenerics.createTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
        .withClassName("Pizza").withOffset(3).withLimit(4).withFields(name)
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
    assertNotNull(get.get("Pizza"));
    assertTrue(get.get("Pizza") instanceof List);
    List getPizza = (List) get.get("Pizza");
    assertEquals(1, getPizza.size());
  }

  @Test
  @Ignore("turned off as openai seems to be quite unstable")
  public void shouldRunGenerativeSearchWithSingleResult() {
    assumeTrue("OpenAI Api Key has to be configured to run the test", StringUtils.isNotBlank(openAIApiKey));

    // given
    WeaviateClient client = createClientWithOpenAIHeader();
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    Field name = Field.builder().name("name").build();
    GenerativeSearchBuilder generativeSearch = GenerativeSearchBuilder.builder()
        .singleResultPrompt("Describe this pizza : {name}")
        .build();

    // when
    Result<GraphQLResponse> result = client.graphQL().get()
        .withClassName("Pizza")
        .withFields(name)
        .withGenerativeSearch(generativeSearch)
        .run();
    testGenerics.cleanupWeaviate(client);

    // then
    List<Map<String, Object>> pizzas = extractResult(result, "Pizza");
    assertThat(pizzas).hasSize(4);
    for (Map<String, Object> pizza : pizzas) {
      assertThat(pizza.get("_additional")).isNotNull().isInstanceOf(Map.class);
      Map<String, Object> additional = (Map<String, Object>) pizza.get("_additional");

      assertThat(additional.get("generate")).isNotNull().isInstanceOf(Map.class);
      Map<String, String> generate = (Map<String, String>) additional.get("generate");

      assertThat(generate).containsOnlyKeys("error", "singleResult");
      assertThat(generate.get("error")).isNull();
      assertThat(generate.get("singleResult")).isNotBlank();
    }
  }

  @Test
  @Ignore("turned off as openai seems to be quite unstable")
  public void shouldRunGenerativeSearchWithGroupedResult() {
    assumeTrue("OpenAI Api Key has to be configured to run the test", StringUtils.isNotBlank(openAIApiKey));

    // given
    WeaviateClient client = createClientWithOpenAIHeader();
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    Field name = Field.builder().name("name").build();
    GenerativeSearchBuilder generativeSearch = GenerativeSearchBuilder.builder()
        .groupedResultTask("Describe these pizzas")
        .build();

    // when
    Result<GraphQLResponse> result = client.graphQL().get()
        .withClassName("Pizza")
        .withFields(name)
        .withGenerativeSearch(generativeSearch)
        .run();
    testGenerics.cleanupWeaviate(client);

    // then
    List<Map<String, Object>> pizzas = extractResult(result, "Pizza");
    assertThat(pizzas).hasSize(4);
    for (int i = 0; i < pizzas.size(); i++) {
      Map<String, Object> pizza = pizzas.get(i);

      if (i == 0) {
        assertThat(pizza.get("_additional")).isNotNull().isInstanceOf(Map.class);
        Map<String, Object> additional = (Map<String, Object>) pizza.get("_additional");

        assertThat(additional.get("generate")).isNotNull().isInstanceOf(Map.class);
        Map<String, String> generate = (Map<String, String>) additional.get("generate");

        assertThat(generate).containsOnlyKeys("error", "groupedResult");
        assertThat(generate.get("error")).isNull();
        assertThat(generate.get("groupedResult")).isNotBlank();
      } else {
        assertThat(pizza.get("_additional")).isNull();
      }
    }
  }

  @Test
  @Ignore("turned off as openai seems to be quite unstable")
  public void shouldRunGenerativeSearchWithGroupedResultAndProperties() {
    assumeTrue("OpenAI Api Key has to be configured to run the test", StringUtils.isNotBlank(openAIApiKey));

    // given
    WeaviateClient client = createClientWithOpenAIHeader();
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    Field name = Field.builder().name("name").build();
    GenerativeSearchBuilder generativeSearch = GenerativeSearchBuilder.builder()
        .groupedResultTask("Describe these pizzas")
        .groupedResultProperties(new String[] { "name", "description" })
        .build();

    // when
    Result<GraphQLResponse> result = client.graphQL().get()
        .withClassName("Pizza")
        .withFields(name)
        .withGenerativeSearch(generativeSearch)
        .run();
    testGenerics.cleanupWeaviate(client);

    // then
    List<Map<String, Object>> pizzas = extractResult(result, "Pizza");
    assertThat(pizzas).hasSize(4);
    for (int i = 0; i < pizzas.size(); i++) {
      Map<String, Object> pizza = pizzas.get(i);

      if (i == 0) {
        assertThat(pizza.get("_additional")).isNotNull().isInstanceOf(Map.class);
        Map<String, Object> additional = (Map<String, Object>) pizza.get("_additional");

        assertThat(additional.get("generate")).isNotNull().isInstanceOf(Map.class);
        Map<String, String> generate = (Map<String, String>) additional.get("generate");

        assertThat(generate).containsOnlyKeys("error", "groupedResult");
        assertThat(generate.get("error")).isNull();
        assertThat(generate.get("groupedResult")).isNotBlank();
      } else {
        assertThat(pizza.get("_additional")).isNull();
      }
    }
  }

  @Test
  @Ignore("turned off as openai seems to be quite unstable")
  public void shouldRunGenerativeSearchWithBothSingleAndGroupedResults() {
    assumeTrue("OpenAI Api Key has to be configured to run the test", StringUtils.isNotBlank(openAIApiKey));

    // given
    WeaviateClient client = createClientWithOpenAIHeader();
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    testGenerics.createTestSchemaAndData(client);

    Field name = Field.builder().name("name").build();
    GenerativeSearchBuilder generativeSearch = GenerativeSearchBuilder.builder()
        .singleResultPrompt("Describe this pizza : {name}")
        .groupedResultTask("Describe these pizzas")
        .build();

    // when
    Result<GraphQLResponse> result = client.graphQL().get()
        .withClassName("Pizza")
        .withFields(name)
        .withGenerativeSearch(generativeSearch)
        .run();
    testGenerics.cleanupWeaviate(client);

    // then
    List<Map<String, Object>> pizzas = extractResult(result, "Pizza");
    assertThat(pizzas).hasSize(4);
    for (int i = 0; i < pizzas.size(); i++) {
      Map<String, Object> pizza = pizzas.get(i);

      assertThat(pizza.get("_additional")).isNotNull().isInstanceOf(Map.class);
      Map<String, Object> additional = (Map<String, Object>) pizza.get("_additional");

      assertThat(additional.get("generate")).isNotNull().isInstanceOf(Map.class);
      Map<String, String> generate = (Map<String, String>) additional.get("generate");

      assertThat(generate).containsOnlyKeys("error", "singleResult", "groupedResult");
      assertThat(generate.get("error")).isNull();
      assertThat(generate.get("singleResult")).isNotBlank();

      if (i == 0) {
        assertThat(generate.get("groupedResult")).isNotBlank();
      } else {
        assertThat(generate.get("groupedResult")).isNull();
      }
    }
  }

  private WeaviateClient createClientWithOpenAIHeader() {
    Map<String, String> headers = new HashMap<>();
    headers.put("X-OpenAI-Api-Key", openAIApiKey);

    Config config = new Config("http", address, headers);
    return new WeaviateClient(config);
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
    for (int i = 0; i < pizzas.size(); i++) {
      assertPizzaName(expectedPizzas[i], pizzas, i);
    }
  }

  @Test
  public void testGraphQLGetWithGroupBy() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);

    // hits
    Field[] hits = new Field[] {
        Field.builder()
            .name("ofDocument")
            .fields(new Field[] {
                Field.builder().name("... on Document")
                    .fields(new Field[] { Field.builder().name("_additional{id}").build() }).build()
            })
            .build(),
        Field.builder().name("_additional{id distance}").build(),
    };
    // group
    Field group = Field.builder()
        .name("group")
        .fields(new Field[] {
            Field.builder().name("id").build(),
            Field.builder().name("groupedBy")
                .fields(new Field[] {
                    Field.builder().name("value").build(),
                    Field.builder().name("path").build(),
                }).build(),
            Field.builder().name("count").build(),
            Field.builder().name("maxDistance").build(),
            Field.builder().name("minDistance").build(),
            Field.builder().name("hits").fields(hits).build(),
        }).build();
    // _additional
    Field _additional = Field.builder().name("_additional").fields(new Field[] { group }).build();
    // Property that we group by
    Field ofDocument = Field.builder().name("ofDocument{__typename}").build();
    // filter arguments
    GroupByArgument groupBy = client.graphQL().arguments().groupByArgBuilder()
        .path(new String[] { "ofDocument" }).groups(3).objectsPerGroup(10).build();
    NearObjectArgument nearObject = client.graphQL().arguments().nearObjectArgBuilder()
        .id("00000000-0000-0000-0000-000000000001").build();
    // when
    testData.createAndInsertData(client);
    Result<GraphQLResponse> groupByResult = client.graphQL().get()
        .withClassName(testData.PASSAGE)
        .withNearObject(nearObject)
        .withGroupBy(groupBy)
        .withFields(ofDocument, _additional).run();
    testData.cleanupWeaviate(client);
    // then
    assertThat(groupByResult).isNotNull();
    assertThat(groupByResult.getError()).isNull();
    assertThat(groupByResult.getResult()).isNotNull();
    List<Map<String, Object>> result = extractResult(groupByResult, testData.PASSAGE);
    assertThat(result).isNotNull().hasSize(3);
    List<Group> groups = getGroups(result);
    assertThat(groups).isNotNull().hasSize(3);
    for (int i = 0; i < 3; i++) {
      assertThat(groups.get(i).minDistance).isEqualTo(groups.get(i).getHits().get(0).get_additional().getDistance());
      assertThat(groups.get(i).maxDistance)
          .isEqualTo(groups.get(i).getHits().get(groups.get(i).getHits().size() - 1).get_additional().getDistance());
    }
    checkGroupElements(expectedHitsA, groups.get(0).getHits());
    checkGroupElements(expectedHitsB, groups.get(1).getHits());
  }

  @Test
  public void testGraphQLGetWithGroupByWithHybrid() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);

    // hits
    Field[] hits = new Field[] {
        Field.builder().name("content").build(),
        Field.builder().name("_additional{id distance}").build(),
    };
    // group
    Field group = Field.builder()
        .name("group")
        .fields(new Field[] {
            Field.builder().name("id").build(),
            Field.builder().name("groupedBy")
                .fields(new Field[] {
                    Field.builder().name("value").build(),
                    Field.builder().name("path").build(),
                }).build(),
            Field.builder().name("count").build(),
            Field.builder().name("maxDistance").build(),
            Field.builder().name("minDistance").build(),
            Field.builder().name("hits").fields(hits).build(),
        }).build();
    // _additional
    Field _additional = Field.builder().name("_additional").fields(new Field[] { group }).build();
    // Property that we group by
    Field content = Field.builder().name("content").build();
    // filter arguments
    GroupByArgument groupBy = client.graphQL().arguments().groupByArgBuilder()
        .path(new String[] { "content" }).groups(3).objectsPerGroup(10).build();
    NearTextArgument nearText = NearTextArgument.builder().concepts(new String[] { "Passage content 2" }).build();
    HybridArgument hybrid = HybridArgument.builder()
        .searches(HybridArgument.Searches.builder().nearText(nearText).build())
        .query("Passage content 2")
        .alpha(0.9f)
        .build();
    // when
    testData.createAndInsertData(client);
    Result<GraphQLResponse> groupByResult = client.graphQL().get()
        .withClassName(testData.PASSAGE)
        .withHybrid(hybrid)
        .withGroupBy(groupBy)
        .withFields(content, _additional).run();
    testData.cleanupWeaviate(client);
    // then
    assertThat(groupByResult).isNotNull();
    assertThat(groupByResult.getError()).isNull();
    assertThat(groupByResult.getResult()).isNotNull();
    List<Map<String, Object>> result = extractResult(groupByResult, testData.PASSAGE);
    assertThat(result).isNotNull().hasSize(3);
    List<Group> groups = getGroups(result);
    assertThat(groups).isNotNull().hasSize(3);
    for (int i = 0; i < 3; i++) {
      if (i == 0) {
        assertThat(groups.get(i).groupedBy.value).isEqualTo("Passage content 2");
      }
      assertThat(groups.get(i).minDistance).isEqualTo(groups.get(i).getHits().get(0).get_additional().getDistance());
      assertThat(groups.get(i).maxDistance)
          .isEqualTo(groups.get(i).getHits().get(groups.get(i).getHits().size() - 1).get_additional().getDistance());
    }
  }

  private void assertPizzaName(String name, List pizzas, int position) {
    assertTrue(pizzas.get(position) instanceof Map);
    Map pizza = (Map) pizzas.get(position);
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

  @Test
  public void shouldSupportSearchByUUID() {
    WeaviateClient client = new WeaviateClient(new Config("http", address));

    String className = "ClassUUID";
    WeaviateClass clazz = WeaviateClass.builder()
        .className(className)
        .description("class with uuid properties")
        .properties(Arrays.asList(
            Property.builder()
                .dataType(Collections.singletonList(DataType.UUID))
                .name("uuidProp")
                .build(),
            Property.builder()
                .dataType(Collections.singletonList(DataType.UUID_ARRAY))
                .name("uuidArrayProp")
                .build()))
        .build();

    String id = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> properties = new HashMap<>();
    properties.put("uuidProp", "7aaa79d3-a564-45db-8fa8-c49e20b8a39a");
    properties.put("uuidArrayProp", new String[] {
        "f70512a3-26cb-4ae4-9369-204555917f15",
        "9e516f40-fd54-4083-a476-f4675b2b5f92"
    });

    Result<Boolean> createStatus = client.schema().classCreator()
        .withClass(clazz)
        .run();

    assertThat(createStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

    Result<WeaviateObject> objectStatus = client.data().creator()
        .withClassName(className)
        .withID(id)
        .withProperties(properties)
        .run();

    assertThat(objectStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull();

    Field fieldId = Field.builder()
        .name("_additional")
        .fields(Field.builder().name("id").build())
        .build();
    WhereArgument whereUuid = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "uuidProp" })
            .operator(Operator.Equal)
            .valueText("7aaa79d3-a564-45db-8fa8-c49e20b8a39a")
            .build())
        .build();
    WhereArgument whereUuidArray1 = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "uuidArrayProp" })
            .operator(Operator.Equal)
            .valueText("f70512a3-26cb-4ae4-9369-204555917f15")
            .build())
        .build();
    WhereArgument whereUuidArray2 = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .path(new String[] { "uuidArrayProp" })
            .operator(Operator.Equal)
            .valueText("9e516f40-fd54-4083-a476-f4675b2b5f92")
            .build())
        .build();

    Result<GraphQLResponse> resultUuid = client.graphQL().get()
        .withWhere(whereUuid)
        .withClassName(className)
        .withFields(fieldId)
        .run();
    Result<GraphQLResponse> resultUuidArray1 = client.graphQL().get()
        .withWhere(whereUuidArray1)
        .withClassName(className)
        .withFields(fieldId)
        .run();
    Result<GraphQLResponse> resultUuidArray2 = client.graphQL().get()
        .withWhere(whereUuidArray2)
        .withClassName(className)
        .withFields(fieldId)
        .run();

    assertIds(className, resultUuid, new String[] { id });
    assertIds(className, resultUuidArray1, new String[] { id });
    assertIds(className, resultUuidArray2, new String[] { id });

    Result<Boolean> deleteStatus = client.schema().allDeleter().run();

    assertThat(deleteStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);
  }

  @Test
  public void shouldSupportSearchWithContains() {
    WeaviateClient client = new WeaviateClient(new Config("http", address));
    String className = "ContainsClass";

    Result<Boolean> createResult = client.schema().classCreator()
        .withClass(WeaviateClass.builder()
            .className(className)
            .properties(Arrays.asList(
                Property.builder()
                    .name("bool")
                    .dataType(Collections.singletonList(DataType.BOOLEAN))
                    .build(),
                Property.builder()
                    .name("bools")
                    .dataType(Collections.singletonList(DataType.BOOLEAN_ARRAY))
                    .build(),

                Property.builder()
                    .name("int")
                    .dataType(Collections.singletonList(DataType.INT))
                    .build(),
                Property.builder()
                    .name("ints")
                    .dataType(Collections.singletonList(DataType.INT_ARRAY))
                    .build(),

                Property.builder()
                    .name("number")
                    .dataType(Collections.singletonList(DataType.NUMBER))
                    .build(),
                Property.builder()
                    .name("numbers")
                    .dataType(Collections.singletonList(DataType.NUMBER_ARRAY))
                    .build(),

                Property.builder()
                    .name("string")
                    .dataType(Collections.singletonList(DataType.STRING))
                    .build(),
                Property.builder()
                    .name("strings")
                    .dataType(Collections.singletonList(DataType.STRING_ARRAY))
                    .build(),

                Property.builder()
                    .name("text")
                    .dataType(Collections.singletonList(DataType.TEXT))
                    .build(),
                Property.builder()
                    .name("texts")
                    .dataType(Collections.singletonList(DataType.TEXT_ARRAY))
                    .build(),

                Property.builder()
                    .name("date")
                    .dataType(Collections.singletonList(DataType.DATE))
                    .build(),
                Property.builder()
                    .name("dates")
                    .dataType(Collections.singletonList(DataType.DATE_ARRAY))
                    .build(),

                Property.builder()
                    .name("uuid")
                    .dataType(Collections.singletonList(DataType.UUID))
                    .build(),
                Property.builder()
                    .name("uuids")
                    .dataType(Collections.singletonList(DataType.UUID_ARRAY))
                    .build()))
            .build())
        .run();

    assertThat(createResult).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

    String id1 = "00000000-0000-0000-0000-000000000001";
    String id2 = "00000000-0000-0000-0000-000000000002";
    String id3 = "00000000-0000-0000-0000-000000000003";

    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    Calendar cal1 = Calendar.getInstance();
    cal1.set(2023, Calendar.JANUARY, 15, 17, 1, 2);
    Date date1 = cal1.getTime();
    Calendar cal2 = Calendar.getInstance();
    cal2.set(2023, Calendar.FEBRUARY, 15, 17, 1, 2);
    Date date2 = cal2.getTime();
    Calendar cal3 = Calendar.getInstance();
    cal3.set(2023, Calendar.MARCH, 15, 17, 1, 2);
    Date date3 = cal3.getTime();

    String[] ids = new String[] {
        id1, id2, id3
    };
    Boolean[] bools = new Boolean[] {
        true, false, true
    };
    Boolean[][] boolsArray = new Boolean[][] {
        { true, false, true },
        { true, false },
        { true },
    };
    Integer[] ints = new Integer[] {
        1, 2, 3
    };
    Integer[][] intsArray = new Integer[][] {
        { 1, 2, 3 },
        { 1, 2 },
        { 1 },
    };
    Double[] numbers = new Double[] {
        1.1, 2.2, 3.3
    };
    Double[][] numbersArray = new Double[][] {
        { 1.1, 2.2, 3.3 },
        { 1.1, 2.2 },
        { 1.1 },
    };
    String[] strings = new String[] {
        "string1", "string2", "string3"
    };
    String[][] stringsArray = new String[][] {
        { "string1", "string2", "string3" },
        { "string1", "string2" },
        { "string1" },
    };
    String[] texts = new String[] {
        "text1", "text2", "text3"
    };
    String[][] textsArray = new String[][] {
        { "text1", "text2", "text3" },
        { "text1", "text2" },
        { "text1" },
    };
    Date[] dates = new Date[] {
        date1, date2, date3
    };
    Date[][] datesArray = new Date[][] {
        { date1, date2, date3 },
        { date1, date2 },
        { date1 },
    };
    String[] uuids = new String[] {
        id1, id2, id3
    };
    String[][] uuidsArray = new String[][] {
        { id1, id2, id3 },
        { id1, id2 },
        { id1 },
    };

    Function<Date, String> formatDate = date -> DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");

    WeaviateObject[] objects = IntStream.range(0, ids.length).mapToObj(i -> {
      Map<String, Object> props = new HashMap<>();
      props.put("bool", bools[i]);
      props.put("bools", boolsArray[i]);
      props.put("int", ints[i]);
      props.put("ints", intsArray[i]);
      props.put("number", numbers[i]);
      props.put("numbers", numbersArray[i]);
      props.put("string", strings[i]);
      props.put("strings", stringsArray[i]);
      props.put("text", texts[i]);
      props.put("texts", textsArray[i]);
      props.put("date", formatDate.apply(dates[i]));
      props.put("dates", Arrays.stream(datesArray[i]).map(formatDate).toArray(String[]::new));
      props.put("uuid", uuids[i]);
      props.put("uuids", uuidsArray[i]);

      return WeaviateObject.builder()
          .className(className)
          .id(ids[i])
          .properties(props)
          .build();
    }).toArray(WeaviateObject[]::new);

    Result<ObjectGetResponse[]> batchResult = client.batch().objectsBatcher()
        .withObjects(objects)
        .run();

    assertBatchSuccessful(objects, batchResult);

    BiConsumer<WhereFilter, String[]> runAndAssertExpectedIds = (filter, expectedIds) -> {
      Result<GraphQLResponse> gqlResult = client.graphQL().get()
          .withClassName(className)
          .withWhere(WhereArgument.builder().filter(filter).build())
          .withFields(Field.builder()
              .name("_additional")
              .fields(Field.builder().name("id").build())
              .build(),
              Field.builder().name("bool").build(),
              Field.builder().name("bools").build())
          .run();

      assertIds(className, gqlResult, expectedIds);
    };

    // FIXME: 0 returned
    // runAndAssertExpectedIds.accept(
    // WhereFilter.builder().path("bools").operator(Operator.ContainsAll).valueBoolean(boolsArray[0]).build(),
    // new String[]{id1, id2});
    // FIXME: 0 returned
    // runAndAssertExpectedIds.accept(
    // WhereFilter.builder().path("bools").operator(Operator.ContainsAll).valueBoolean(boolsArray[1]).build(),
    // new String[]{id1, id2});
    // FIXME: 1 returned
    // runAndAssertExpectedIds.accept(
    // WhereFilter.builder().path("bools").operator(Operator.ContainsAll).valueBoolean(boolsArray[2]).build(),
    // new String[]{id1, id2, id3});
    // FIXME: 1 returned
    // runAndAssertExpectedIds.accept(
    // WhereFilter.builder().path("bools").operator(Operator.ContainsAny).valueBoolean(boolsArray[0]).build(),
    // new String[]{id1, id2, id3});
    // FIXME: 1 returned
    // runAndAssertExpectedIds.accept(
    // WhereFilter.builder().path("bools").operator(Operator.ContainsAny).valueBoolean(boolsArray[1]).build(),
    // new String[]{id1, id2, id3});
    // FIXME: 1 returned
    // runAndAssertExpectedIds.accept(
    // WhereFilter.builder().path("bools").operator(Operator.ContainsAny).valueBoolean(boolsArray[2]).build(),
    // new String[]{id1, id2, id3});

    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("ints").operator(Operator.ContainsAll).valueInt(intsArray[0]).build(),
        new String[] { id1 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("ints").operator(Operator.ContainsAll).valueInt(intsArray[1]).build(),
        new String[] { id1, id2 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("ints").operator(Operator.ContainsAll).valueInt(intsArray[2]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("ints").operator(Operator.ContainsAny).valueInt(intsArray[0]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("ints").operator(Operator.ContainsAny).valueInt(intsArray[1]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("ints").operator(Operator.ContainsAny).valueInt(intsArray[2]).build(),
        new String[] { id1, id2, id3 });

    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("numbers").operator(Operator.ContainsAll).valueNumber(numbersArray[0]).build(),
        new String[] { id1 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("numbers").operator(Operator.ContainsAll).valueNumber(numbersArray[1]).build(),
        new String[] { id1, id2 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("numbers").operator(Operator.ContainsAll).valueNumber(numbersArray[2]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("numbers").operator(Operator.ContainsAny).valueNumber(numbersArray[0]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("numbers").operator(Operator.ContainsAny).valueNumber(numbersArray[1]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("numbers").operator(Operator.ContainsAny).valueNumber(numbersArray[2]).build(),
        new String[] { id1, id2, id3 });

    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("strings").operator(Operator.ContainsAll).valueString(stringsArray[0]).build(),
        new String[] { id1 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("strings").operator(Operator.ContainsAll).valueString(stringsArray[1]).build(),
        new String[] { id1, id2 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("strings").operator(Operator.ContainsAll).valueString(stringsArray[2]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("strings").operator(Operator.ContainsAny).valueString(stringsArray[0]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("strings").operator(Operator.ContainsAny).valueString(stringsArray[1]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("strings").operator(Operator.ContainsAny).valueString(stringsArray[2]).build(),
        new String[] { id1, id2, id3 });

    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("texts").operator(Operator.ContainsAll).valueText(textsArray[0]).build(),
        new String[] { id1 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("texts").operator(Operator.ContainsAll).valueText(textsArray[1]).build(),
        new String[] { id1, id2 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("texts").operator(Operator.ContainsAll).valueText(textsArray[2]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("texts").operator(Operator.ContainsAny).valueText(textsArray[0]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("texts").operator(Operator.ContainsAny).valueText(textsArray[1]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("texts").operator(Operator.ContainsAny).valueText(textsArray[2]).build(),
        new String[] { id1, id2, id3 });

    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("dates").operator(Operator.ContainsAll).valueDate(datesArray[0]).build(),
        new String[] { id1 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("dates").operator(Operator.ContainsAll).valueDate(datesArray[1]).build(),
        new String[] { id1, id2 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("dates").operator(Operator.ContainsAll).valueDate(datesArray[2]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("dates").operator(Operator.ContainsAny).valueDate(datesArray[0]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("dates").operator(Operator.ContainsAny).valueDate(datesArray[1]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("dates").operator(Operator.ContainsAny).valueDate(datesArray[2]).build(),
        new String[] { id1, id2, id3 });

    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("uuids").operator(Operator.ContainsAll).valueText(uuidsArray[0]).build(),
        new String[] { id1 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("uuids").operator(Operator.ContainsAll).valueText(uuidsArray[1]).build(),
        new String[] { id1, id2 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("uuids").operator(Operator.ContainsAll).valueText(uuidsArray[2]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("uuids").operator(Operator.ContainsAny).valueText(uuidsArray[0]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("uuids").operator(Operator.ContainsAny).valueText(uuidsArray[1]).build(),
        new String[] { id1, id2, id3 });
    runAndAssertExpectedIds.accept(
        WhereFilter.builder().path("uuids").operator(Operator.ContainsAny).valueText(uuidsArray[2]).build(),
        new String[] { id1, id2, id3 });
  }

  protected void assertIds(String className, Result<GraphQLResponse> gqlResult, String[] expectedIds) {
    assertThat(gqlResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull()
        .extracting(GraphQLResponse::getData).isInstanceOf(Map.class)
        .extracting(data -> ((Map<String, Object>) data).get("Get")).isInstanceOf(Map.class)
        .extracting(get -> ((Map<String, Object>) get).get(className)).isInstanceOf(List.class).asList()
        .hasSize(expectedIds.length);

    List<Map<String, Object>> results = (List<Map<String, Object>>) ((Map<String, Object>) (((Map<String, Object>) (gqlResult
        .getResult().getData())).get(
            "Get")))
        .get(className);
    String[] resultIds = results.stream()
        .map(m -> m.get("_additional"))
        .map(a -> ((Map<String, String>) a).get("id"))
        .toArray(String[]::new);

    assertThat(resultIds).containsExactlyInAnyOrder(expectedIds);
  }

  private void assertBatchSuccessful(WeaviateObject[] objects, Result<ObjectGetResponse[]> batchResult) {
    assertThat(batchResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asInstanceOf(ARRAY)
        .hasSize(objects.length);
    Arrays.stream(batchResult.getResult()).forEach(resp -> assertThat(resp).isNotNull()
        .extracting(ObjectGetResponse::getResult)
        .returns(ObjectGetResponseStatus.SUCCESS, ObjectsGetResponseAO2Result::getStatus));
  }
}
