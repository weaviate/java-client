package io.weaviate.integration.client.graphql;

import com.google.gson.internal.LinkedTreeMap;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerComposeCluster;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClusterGraphQLTest {
  private String address;

  @ClassRule
  public static WeaviateDockerComposeCluster compose = new WeaviateDockerComposeCluster();

  @Before
  public void before() {
    address = compose.getHttpHost0Address();
  }

  @Test
  public void testGraphQLGetUsingConsistencyLevelAll() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
      .name("_additional")
      .fields(new Field[]{Field.builder().name("isConsistent").build()})
      .build();
    // when
    testGenerics.createReplicatedTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
      .withClassName("Pizza").withConsistencyLevel(ConsistencyLevel.ALL)
      .withFields(name, _additional)
      .run();
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
    for (Object pizza : getPizza) {
      LinkedTreeMap pizzaMap = (LinkedTreeMap) pizza;
      LinkedTreeMap additional = (LinkedTreeMap) pizzaMap.get("_additional");
      assertTrue((boolean) additional.get("isConsistent"));
    }

    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testGraphQLGetUsingConsistencyLevelQuorum() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
      .name("_additional")
      .fields(new Field[]{Field.builder().name("isConsistent").build()})
      .build();
    // when
    testGenerics.createReplicatedTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
      .withClassName("Pizza").withConsistencyLevel(ConsistencyLevel.QUORUM)
      .withFields(name, _additional)
      .run();
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
    for (Object pizza : getPizza) {
      LinkedTreeMap pizzaMap = (LinkedTreeMap) pizza;
      LinkedTreeMap additional = (LinkedTreeMap) pizzaMap.get("_additional");
      assertTrue((boolean) additional.get("isConsistent"));
    }

    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testGraphQLGetUsingConsistencyLevelOne() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    Field name = Field.builder().name("name").build();
    Field _additional = Field.builder()
      .name("_additional")
      .fields(new Field[]{Field.builder().name("isConsistent").build()})
      .build();
    // when
    testGenerics.createReplicatedTestSchemaAndData(client);
    Result<GraphQLResponse> result = client.graphQL().get()
      .withClassName("Pizza").withConsistencyLevel(ConsistencyLevel.ONE)
      .withFields(name, _additional)
      .run();
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
    for (Object pizza : getPizza) {
      LinkedTreeMap pizzaMap = (LinkedTreeMap) pizza;
      LinkedTreeMap additional = (LinkedTreeMap) pizzaMap.get("_additional");
      assertTrue((boolean) additional.get("isConsistent"));
    }

    testGenerics.cleanupWeaviate(client);
  }
}
