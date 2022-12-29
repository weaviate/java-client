package technology.semi.weaviate.integration.client.batch;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
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
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClientBatchCreateTest {
  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200))
    .withTailChildContainers(true);
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
  private WeaviateClient client;

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    Config config = new Config("http", host + ":" + port);

    client = new WeaviateClient(config);
    testGenerics.createWeaviateTestSchemaFood(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testBatchCreate() {
    // given
    // objT1
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
    // objT2
    String objT2ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
    Map<String, Object> propertiesSchemaT2 = new HashMap<>();
    propertiesSchemaT2.put("name", "Doener");
    propertiesSchemaT2.put("description", "A innovation, some say revolution, in the pizza industry.");
    WeaviateObject objT2 = WeaviateObject.builder().className("Pizza").id(objT2ID).properties(propertiesSchemaT2).build();
    // objA1
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "ChickenSoup");
    propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    // objA2
    String objA2ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
    Map<String, Object> propertiesSchemaA2 = new HashMap<>();
    propertiesSchemaA2.put("name", "Beautiful");
    propertiesSchemaA2.put("description", "Putting the game of letter soups to a whole new level.");
    WeaviateObject objA2 = WeaviateObject.builder().className("Soup").id(objA2ID).properties(propertiesSchemaA2).build();
    // when
    Result<WeaviateObject> objT1 = client.data().creator()
      .withClassName("Pizza")
      .withID(objTID)
      .withProperties(propertiesSchemaT)
      .run();
    Result<WeaviateObject> objA1 = client.data().creator()
      .withClassName("Soup")
      .withID(objAID)
      .withProperties(propertiesSchemaA)
      .run();
    Result<ObjectGetResponse[]> batchTs = client.batch().objectsBatcher()
      .withObjects(objT1.getResult(), objT2)
      .run();
    Result<ObjectGetResponse[]> batchAs = client.batch().objectsBatcher()
      .withObjects(objA1.getResult(), objA2)
      .run();
    // check if created objects exist
    Result<List<WeaviateObject>> getObjT1 = client.data().objectsGetter().withID(objTID).withClassName("Pizza").run();
    Result<List<WeaviateObject>> getObjT2 = client.data().objectsGetter().withID(objT2ID).withClassName("Pizza").run();
    Result<List<WeaviateObject>> getObjA1 = client.data().objectsGetter().withID(objAID).withClassName("Soup").run();
    Result<List<WeaviateObject>> getObjA2 = client.data().objectsGetter().withID(objA2ID).withClassName("Soup").run();
    // then
    assertNotNull(objT1);
    assertNotNull(objT1.getResult());
    assertEquals(objTID, objT1.getResult().getId());
    assertNotNull(objA1);
    assertNotNull(objA1.getResult());
    assertEquals(objAID, objA1.getResult().getId());
    assertNotNull(batchTs);
    assertNotNull(batchTs.getResult());
    assertEquals(2, batchTs.getResult().length);
    assertNotNull(batchAs);
    assertNotNull(batchAs.getResult());
    assertEquals(2, batchAs.getResult().length);
    assertNotNull(getObjT1);
    assertNotNull(getObjT1.getResult());
    assertEquals(1, getObjT1.getResult().size());
    assertEquals(objTID, getObjT1.getResult().get(0).getId());
    assertNotNull(getObjT2);
    assertNotNull(getObjT2.getResult());
    assertEquals(1, getObjT2.getResult().size());
    assertEquals(objT2ID, getObjT2.getResult().get(0).getId());
    assertNotNull(getObjA1);
    assertNotNull(getObjA1.getResult());
    assertEquals(1, getObjA1.getResult().size());
    assertEquals(objAID, getObjA1.getResult().get(0).getId());
    assertNotNull(getObjA2);
    assertEquals(1, getObjA2.getResult().size());
    assertEquals(objA2ID, getObjA2.getResult().get(0).getId());
  }
}
