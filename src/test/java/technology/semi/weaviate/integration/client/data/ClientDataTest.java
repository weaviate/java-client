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
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
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
    Result<WeaviateObject> objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Result<WeaviateObject> objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> objectsA = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectA);
    assertNotNull(objectA.getResult());
    assertEquals(objAID, objectA.getResult().getId());
    assertNotNull(objectsT);
    assertNotNull(objectsT.getResult());
    assertEquals(1, objectsT.getResult().size());
    assertEquals(objTID, objectsT.getResult().get(0).getId());
    assertNotNull(objectsT.getResult().get(0).getProperties());
    assertEquals(2, objectsT.getResult().get(0).getProperties().size());
    assertEquals("Pizza", objectsT.getResult().get(0).getClassName());
    assertEquals("Hawaii", objectsT.getResult().get(0).getProperties().get("name"));
    assertNotNull(objectsA);
    assertNotNull(objectsA.getResult());
    assertEquals(1, objectsA.getResult().size());
    assertEquals(objAID, objectsA.getResult().get(0).getId());
    assertNotNull(objectsA.getResult().get(0).getProperties());
    assertEquals(2, objectsA.getResult().get(0).getProperties().size());
    assertEquals("Soup", objectsA.getResult().get(0).getClassName());
    assertEquals("ChickenSoup", objectsA.getResult().get(0).getProperties().get("name"));
  }

  @Test
  public void testDataGetActionsThings() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Result<WeaviateObject> pizzaObj1 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Margherita");
      put("description", "plain");
    }}).run();
    Result<WeaviateObject> pizzaObj2 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Pepperoni");
      put("description", "meat");
    }}).run();
    Result<WeaviateObject> soupObj1 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Chicken");
      put("description", "plain");
    }}).run();
    Result<WeaviateObject> soupObj2 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
      put("name", "Tofu");
      put("description", "vegetarian");
    }}).run();
    Result<List<WeaviateObject>> objects = client.data().objectsGetter().run();
    Result<List<WeaviateObject>> objects1 = client.data().objectsGetter().withLimit(1).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(pizzaObj1);
    assertNotNull(pizzaObj1.getResult());
    assertNotNull(pizzaObj1.getResult().getId());
    assertNotNull(pizzaObj2);
    assertNotNull(pizzaObj2.getResult());
    assertNotNull(pizzaObj2.getResult().getId());
    assertNotNull(soupObj1);
    assertNotNull(soupObj1.getResult());
    assertNotNull(soupObj1.getResult().getId());
    assertNotNull(soupObj2);
    assertNotNull(soupObj2.getResult());
    assertNotNull(soupObj2.getResult().getId());
    assertNotNull(objects);
    assertNotNull(objects.getResult());
    assertEquals(4, objects.getResult().size());
    assertNotNull(objects1);
    assertNotNull(objects1.getResult());
    assertEquals(1, objects1.getResult().size());
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
    Result<WeaviateObject> objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Result<WeaviateObject> objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> objectsA = client.data().objectsGetter().withID(objAID).run();
    Result<List<WeaviateObject>> objsAdditionalT = client.data()
            .objectsGetter().withID(objTID)
            .withAdditional("classification")
            .withAdditional("nearestNeighbors")
            .withVector()
            .run();
    Result<List<WeaviateObject>> objsAdditionalA = client.data()
            .objectsGetter().withID(objAID)
            .withAdditional("classification")
            .withAdditional("nearestNeighbors")
            .withAdditional("interpretation")
            .withVector()
            .run();
    Result<List<WeaviateObject>> objsAdditionalA1 = client.data()
            .objectsGetter().withID(objAID)
            .run();
    Result<List<WeaviateObject>> objsAdditionalA2 = client.data()
            .objectsGetter().withID(objAID)
            .withAdditional("interpretation")
            .run();
    Result<List<WeaviateObject>> objsAdditionalAError = client.data()
            .objectsGetter().withID(objAID)
            .withAdditional("featureProjection")
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectA);
    assertNotNull(objectA.getResult());
    assertEquals(objAID, objectA.getResult().getId());
    assertNotNull(objectsT);
    assertNotNull(objectsT.getResult());
    assertEquals(1, objectsT.getResult().size());
    assertNull(objectsT.getResult().get(0).getAdditional());
    assertNotNull(objectsA);
    assertNotNull(objectsA.getResult());
    assertEquals(1, objectsA.getResult().size());
    assertNull(objectsA.getResult().get(0).getAdditional());
    assertNotNull(objsAdditionalT);
    assertNotNull(objsAdditionalT.getResult());
    assertEquals(1, objsAdditionalT.getResult().size());
    assertNotNull(objsAdditionalT.getResult().get(0).getAdditional());
    assertEquals(2, objsAdditionalT.getResult().get(0).getAdditional().size());
    assertNull(objsAdditionalT.getResult().get(0).getAdditional().get("classification"));
    assertNotNull(objsAdditionalT.getResult().get(0).getAdditional().get("nearestNeighbors"));
    assertNotNull(objsAdditionalT.getResult().get(0).getVector());
    assertNotNull(objsAdditionalA);
    assertNotNull(objsAdditionalA.getResult());
    assertEquals(1, objsAdditionalA.getResult().size());
    assertNotNull(objsAdditionalA.getResult().get(0).getAdditional());
    assertEquals(3, objsAdditionalA.getResult().get(0).getAdditional().size());
    assertNull(objsAdditionalA.getResult().get(0).getAdditional().get("classification"));
    assertNotNull(objsAdditionalA.getResult().get(0).getAdditional().get("nearestNeighbors"));
    assertNotNull(objsAdditionalA.getResult().get(0).getAdditional().get("interpretation"));
    assertNotNull(objsAdditionalA.getResult().get(0).getVector());
    assertNotNull(objsAdditionalA1.getResult());
    assertEquals(1, objsAdditionalA1.getResult().size());
    assertNull(objsAdditionalA1.getResult().get(0).getAdditional());
    assertNotNull(objsAdditionalA2.getResult());
    assertEquals(1, objsAdditionalA2.getResult().size());
    assertNotNull(objsAdditionalA2.getResult().get(0).getAdditional());
    assertEquals(1, objsAdditionalA2.getResult().get(0).getAdditional().size());
    assertNotNull(objsAdditionalA2.getResult().get(0).getAdditional().get("interpretation"));
    assertNotNull(objsAdditionalAError);
    assertNull(objsAdditionalAError.getResult());
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
    Result<WeaviateObject> objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Result<WeaviateObject> objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Result<Boolean> deleteObjT = client.data().deleter().withID(objTID).run();
    Result<List<WeaviateObject>> objTlist = client.data().objectsGetter().withID(objTID).run();
    Result<Boolean> deleteObjA = client.data().deleter().withID(objAID).run();
    Result<List<WeaviateObject>> objAlist = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectA);
    assertNotNull(objectA.getResult());
    assertEquals(objAID, objectA.getResult().getId());
    assertNotNull(deleteObjT);
    assertTrue(deleteObjT.getResult());
    assertNotNull(objTlist);
    assertNotNull(objTlist.getResult());
    assertNotNull(deleteObjA);
    assertTrue(deleteObjA.getResult());
    assertNotNull(objAlist);
    assertNotNull(objAlist.getResult());
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
    Result<WeaviateObject> objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Result<WeaviateObject> objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Result<Boolean> updateObjectT = client.data().updater()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("name", "Hawaii");
              put("description", "Universally accepted to be the best pizza ever created.");
            }})
            .run();
    Result<Boolean> updateObjectA = client.data().updater()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("name", "ChickenSoup");
              put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
            }})
            .run();
    Result<List<WeaviateObject>> updatedObjsT = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> updatedObjsA = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectA);
    assertNotNull(objectA.getResult());
    assertEquals(objAID, objectA.getResult().getId());
    assertNotNull(updateObjectT);
    assertTrue(updateObjectT.getResult());
    assertNotNull(updateObjectA);
    assertTrue(updateObjectA.getResult());
    assertNotNull(updatedObjsT);
    assertNotNull(updatedObjsT.getResult());
    assertEquals(1, updatedObjsT.getResult().size());
    assertEquals("Hawaii", updatedObjsT.getResult().get(0).getProperties().get("name"));
    assertEquals("Universally accepted to be the best pizza ever created.", updatedObjsT.getResult().get(0).getProperties().get("description"));
    assertNotNull(updatedObjsA);
    assertNotNull(updatedObjsA.getResult());
    assertEquals(1, updatedObjsA.getResult().size());
    assertEquals("ChickenSoup", updatedObjsA.getResult().get(0).getProperties().get("name"));
    assertEquals("Used by humans when their inferior genetics are attacked by microscopic organisms.", updatedObjsA.getResult().get(0).getProperties().get("description"));
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
    Result<WeaviateObject> objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Result<WeaviateObject> objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Result<Boolean> mergeObjectT = client.data().updater()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("description", "Universally accepted to be the best pizza ever created.");
            }})
            .withMerge()
            .run();
    Result<Boolean> mergeObjectA = client.data().updater()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
            }})
            .withMerge()
            .run();
    Result<List<WeaviateObject>> mergedObjsT = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> mergeddObjsA = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectA);
    assertNotNull(objectA.getResult());
    assertEquals(objAID, objectA.getResult().getId());
    assertNotNull(mergeObjectT);
    assertTrue(mergeObjectT.getResult());
    assertNotNull(mergeObjectA);
    assertTrue(mergeObjectA.getResult());
    assertNotNull(mergedObjsT);
    assertNotNull(mergedObjsT.getResult());
    assertEquals(1, mergedObjsT.getResult().size());
    assertEquals("Hawaii", mergedObjsT.getResult().get(0).getProperties().get("name"));
    assertEquals("Universally accepted to be the best pizza ever created.", mergedObjsT.getResult().get(0).getProperties().get("description"));
    assertNotNull(mergeddObjsA);
    assertNotNull(mergeddObjsA.getResult());
    assertEquals(1, mergeddObjsA.getResult().size());
    assertEquals("ChickenSoup", mergeddObjsA.getResult().get(0).getProperties().get("name"));
    assertEquals("Used by humans when their inferior genetics are attacked by microscopic organisms.", mergeddObjsA.getResult().get(0).getProperties().get("description"));
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
    Result<Boolean> validateObjT = client.data().validator()
            .withClassName("Pizza")
            .withID(objTID)
            .withSchema(propertiesSchemaT)
            .run();
    Result<Boolean> validateObjA = client.data().validator()
            .withClassName("Soup")
            .withID(objAID)
            .withSchema(propertiesSchemaA)
            .run();
    propertiesSchemaT.put("test", "not existing property");
    Result<Boolean> validateObjT1 = client.data().validator()
            .withClassName("Pizza")
            .withID(objTID)
            .withSchema(propertiesSchemaT)
            .run();
    propertiesSchemaA.put("test", "not existing property");
    Result<Boolean> validateObjA1 = client.data().validator()
            .withClassName("Pizza")
            .withID(objTID)
            .withSchema(propertiesSchemaT)
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(validateObjT);
    assertTrue(validateObjT.getResult());
    assertNotNull(validateObjA);
    assertTrue(validateObjA.getResult());
    assertNotNull(validateObjT1);
    assertNotNull(validateObjT1.getError());
    assertEquals("invalid object: no such prop with name 'test' found in class 'Pizza' in the schema." +
                    " Check your schema files for which properties in this class are available",
            validateObjT1.getError().getMessages().get(0).getMessage());
    assertNotNull(validateObjA1);
    assertNotNull(validateObjA1.getError());
    assertEquals("invalid object: no such prop with name 'test' found in class 'Pizza' in the schema." +
                    " Check your schema files for which properties in this class are available",
            validateObjA1.getError().getMessages().get(0).getMessage());
  }

  @Test
  public void testDataGetWithAdditionalError() throws IOException {
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
    Result<WeaviateObject> objectT = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Result<WeaviateObject> objectA = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    Result<List<WeaviateObject>> objsAdditionalT = client.data()
            .objectsGetter().withID(objTID)
            .withAdditional("featureProjection")
            .withVector()
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertNotNull(objectA);
    assertNotNull(objectA.getResult());
    assertNotNull(objsAdditionalT);
    assertNotNull(objsAdditionalT.getError());
    assertNotNull(objsAdditionalT.getError().getMessages());
    assertEquals("get extend: unknown capability: featureProjection", objsAdditionalT.getError().getMessages().get(0).getMessage());
  }
}
