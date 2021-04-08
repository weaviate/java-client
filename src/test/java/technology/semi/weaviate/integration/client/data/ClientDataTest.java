package technology.semi.weaviate.integration.client.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.data.model.Object;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClientDataTest {
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
  public void testDataCreate() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
    Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "ChickenSoup");
    propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Object objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Object objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    List<Object> objectsT = client.data().objectsGetter().withID(objTID).run();
    List<Object> objectsA = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertEquals(objTID, objectT.getId());
    assertNotNull(objectA);
    assertEquals(objAID, objectA.getId());
    assertNotNull(objectsT);
    assertEquals(1, objectsT.size());
    assertEquals(objTID, objectsT.get(0).getId());
    assertNotNull(objectsT.get(0).getProperties());
    assertEquals(2, objectsT.get(0).getProperties().size());
    assertEquals("Pizza", objectsT.get(0).getClassName());
    assertEquals("Hawaii", objectsT.get(0).getProperties().get("name"));
    assertNotNull(objectsA);
    assertEquals(1, objectsA.size());
    assertEquals(objAID, objectsA.get(0).getId());
    assertNotNull(objectsA.get(0).getProperties());
    assertEquals(2, objectsA.get(0).getProperties().size());
    assertEquals("Soup", objectsA.get(0).getClassName());
    assertEquals("ChickenSoup", objectsA.get(0).getProperties().get("name"));
  }

  @Test
  public void testDataGetActionsThings() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Object pizzaObj1 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Margherita");
      put("description", "plain");
    }}).run();
    Object pizzaObj2 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Pepperoni");
      put("description", "meat");
    }}).run();
    Object soupObj1 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Chicken");
      put("description", "plain");
    }}).run();
    Object soupObj2 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Tofu");
      put("description", "vegetarian");
    }}).run();
    List<Object> objects = client.data().objectsGetter().run();
    List<Object> objects1 = client.data().objectsGetter().withLimit(1).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(pizzaObj1);
    assertNotNull(pizzaObj1.getId());
    assertNotNull(pizzaObj2);
    assertNotNull(pizzaObj2.getId());
    assertNotNull(soupObj1);
    assertNotNull(soupObj1.getId());
    assertNotNull(soupObj2);
    assertNotNull(soupObj2.getId());
    assertNotNull(objects);
    assertEquals(4, objects.size());
    assertNotNull(objects1);
    assertEquals(1, objects1.size());
  }

  @Test
  public void testDataGetWithAdditional() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
    Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "ChickenSoup");
    propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Object objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Object objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    List<Object> objectsT = client.data().objectsGetter().withID(objTID).run();
    List<Object> objectsA = client.data().objectsGetter().withID(objAID).run();
    List<Object> objsAdditionalT = client.data()
            .objectsGetter().withID(objTID)
            .withAdditional("classification")
            .withAdditional("nearestNeighbors")
            .withVector()
            .run();
    List<Object> objsAdditionalA = client.data()
            .objectsGetter().withID(objAID)
            .withAdditional("classification")
            .withAdditional("nearestNeighbors")
            .withAdditional("interpretation")
            .withVector()
            .run();
    List<Object> objsAdditionalA1 = client.data()
            .objectsGetter().withID(objAID)
            .run();
    List<Object> objsAdditionalA2 = client.data()
            .objectsGetter().withID(objAID)
            .withAdditional("interpretation")
            .run();
    List<Object> objsAdditionalAError = client.data()
            .objectsGetter().withID(objAID)
            .withAdditional("featureProjection")
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertEquals(objTID, objectT.getId());
    assertNotNull(objectA);
    assertEquals(objAID, objectA.getId());
    assertNotNull(objectsT);
    assertEquals(1, objectsT.size());
    assertNull(objectsT.get(0).getAdditional());
    assertNotNull(objectsA);
    assertEquals(1, objectsA.size());
    assertNull(objectsA.get(0).getAdditional());
    assertNotNull(objsAdditionalT);
    assertEquals(1, objsAdditionalT.size());
    assertNotNull(objsAdditionalT.get(0).getAdditional());
    assertEquals(2, objsAdditionalT.get(0).getAdditional().size());
    assertNull(objsAdditionalT.get(0).getAdditional().get("classification"));
    assertNotNull(objsAdditionalT.get(0).getAdditional().get("nearestNeighbors"));
    assertNotNull(objsAdditionalT.get(0).getVector());
    assertNotNull(objsAdditionalA);
    assertEquals(1, objsAdditionalA.size());
    assertNotNull(objsAdditionalA.get(0).getAdditional());
    assertEquals(3, objsAdditionalA.get(0).getAdditional().size());
    assertNull(objsAdditionalA.get(0).getAdditional().get("classification"));
    assertNotNull(objsAdditionalA.get(0).getAdditional().get("nearestNeighbors"));
    assertNotNull(objsAdditionalA.get(0).getAdditional().get("interpretation"));
    assertNotNull(objsAdditionalA.get(0).getVector());
    assertEquals(1, objsAdditionalA1.size());
    assertNull(objsAdditionalA1.get(0).getAdditional());
    assertEquals(1, objsAdditionalA2.size());
    assertNotNull(objsAdditionalA2.get(0).getAdditional());
    assertEquals(1, objsAdditionalA2.get(0).getAdditional().size());
    assertNotNull(objsAdditionalA2.get(0).getAdditional().get("interpretation"));
    assertNull(objsAdditionalAError);
  }

  @Test
  public void testDataDelete() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
    Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "ChickenSoup");
    propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Object objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Object objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Boolean deleteObjT = client.data().deleter().withID(objTID).run();
    List<Object> objTlist = client.data().objectsGetter().withID(objTID).run();
    Boolean deleteObjA = client.data().deleter().withID(objAID).run();
    List<Object> objAlist = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertEquals(objTID, objectT.getId());
    assertNotNull(objectA);
    assertEquals(objAID, objectA.getId());
    assertTrue(deleteObjT);
    assertNull(objTlist);
    assertTrue(deleteObjA);
    assertNull(objAlist);
  }

  @Test
  public void testDataUpdate() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Random");
    propertiesSchemaT.put("description", "Missing description");
    Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "water");
    propertiesSchemaA.put("description", "missing description");
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Object objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Object objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Boolean updateObjectT = client.data().updater()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("name", "Hawaii");
              put("description", "Universally accepted to be the best pizza ever created.");
            }})
            .run();
    Boolean updateObjectA = client.data().updater()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("name", "ChickenSoup");
              put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
            }})
            .run();
    List<Object> updatedObjsT = client.data().objectsGetter().withID(objTID).run();
    List<Object> updatedObjsA = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertEquals(objTID, objectT.getId());
    assertNotNull(objectA);
    assertEquals(objAID, objectA.getId());
    assertTrue(updateObjectT);
    assertTrue(updateObjectA);
    assertNotNull(updatedObjsT);
    assertEquals(1, updatedObjsT.size());
    assertEquals("Hawaii", updatedObjsT.get(0).getProperties().get("name"));
    assertEquals("Universally accepted to be the best pizza ever created.", updatedObjsT.get(0).getProperties().get("description"));
    assertNotNull(updatedObjsA);
    assertEquals(1, updatedObjsA.size());
    assertEquals("ChickenSoup", updatedObjsA.get(0).getProperties().get("name"));
    assertEquals("Used by humans when their inferior genetics are attacked by microscopic organisms.", updatedObjsA.get(0).getProperties().get("description"));
  }

  @Test
  public void testDataMerge() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Missing description");
    Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "ChickenSoup");
    propertiesSchemaA.put("description", "missing description");
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Object objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Object objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Boolean mergeObjectT = client.data().updater()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("description", "Universally accepted to be the best pizza ever created.");
            }})
            .withMerge()
            .run();
    Boolean mergeObjectA = client.data().updater()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
            }})
            .withMerge()
            .run();
    List<Object> mergedObjsT = client.data().objectsGetter().withID(objTID).run();
    List<Object> mergeddObjsA = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertEquals(objTID, objectT.getId());
    assertNotNull(objectA);
    assertEquals(objAID, objectA.getId());
    assertTrue(mergeObjectT);
    assertTrue(mergeObjectA);
    assertNotNull(mergedObjsT);
    assertEquals(1, mergedObjsT.size());
    assertEquals("Hawaii", mergedObjsT.get(0).getProperties().get("name"));
    assertEquals("Universally accepted to be the best pizza ever created.", mergedObjsT.get(0).getProperties().get("description"));
    assertNotNull(mergeddObjsA);
    assertEquals(1, mergeddObjsA.size());
    assertEquals("ChickenSoup", mergeddObjsA.get(0).getProperties().get("name"));
    assertEquals("Used by humans when their inferior genetics are attacked by microscopic organisms.", mergeddObjsA.get(0).getProperties().get("description"));
  }

  @Test
  public void testDataValidate() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
    Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "ChickenSoup");
    propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Boolean validateObjT = client.data().validator()
            .withClassName("Pizza")
            .withID(objTID)
            .withSchema(propertiesSchemaT)
            .run();
    Boolean validateObjA = client.data().validator()
            .withClassName("Soup")
            .withID(objAID)
            .withSchema(propertiesSchemaA)
            .run();
    propertiesSchemaT.put("test", "not existing property");
    Boolean validateObjT1 = client.data().validator()
            .withClassName("Pizza")
            .withID(objTID)
            .withSchema(propertiesSchemaT)
            .run();
    propertiesSchemaA.put("test", "not existing property");
    Boolean validateObjA1 = client.data().validator()
            .withClassName("Pizza")
            .withID(objTID)
            .withSchema(propertiesSchemaT)
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertTrue(validateObjT);
    assertTrue(validateObjA);
    assertFalse(validateObjT1);
    assertFalse(validateObjA1);
  }
}
