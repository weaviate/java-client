package io.weaviate.integration.client.data;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ClientDataTest {
  private String address;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();
  }

  @Test
  public void testDataCreate() {
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
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();
    Result<WeaviateObject> objectA = client.data().creator()
      .withClassName("Soup")
      .withID(objAID)
      .withProperties(propertiesSchemaA)
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run();
    Result<List<WeaviateObject>> objectsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run();
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
  public void testDataCreateWithSpecialCharacters() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String name = "Zażółć gęślą jaźń";
    String description = "test äüëö";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", name);
    propertiesSchemaT.put("description", description);
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Result<WeaviateObject> objectT = client.data().creator()
      .withClassName("Pizza")
      .withID(objTID)
      .withProperties(propertiesSchemaT)
      .run();
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectsT);
    assertNotNull(objectsT.getResult());
    assertEquals(1, objectsT.getResult().size());
    assertEquals(objTID, objectsT.getResult().get(0).getId());
    assertNotNull(objectsT.getResult().get(0).getProperties());
    assertEquals(2, objectsT.getResult().get(0).getProperties().size());
    assertEquals("Pizza", objectsT.getResult().get(0).getClassName());
    assertEquals(name, objectsT.getResult().get(0).getProperties().get("name"));
    assertEquals(description, objectsT.getResult().get(0).getProperties().get("description"));
  }

  @Test
  public void testDataGetActionsThings() {
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
    Result<List<WeaviateObject>> objects1 = client.data().objectsGetter().withClassName("Pizza").withLimit(1).run();
    assertNull(objects1.getError());
    assertEquals(1l, objects1.getResult().size());
    String firstPizzaID = objects1.getResult().get(0).getId();
    Result<List<WeaviateObject>> afterFirstPizzaObjects = client.data()
      .objectsGetter()
      .withClassName("Pizza").withAfter(firstPizzaID).withLimit(1)
      .run();

    testGenerics.cleanupWeaviate(client);
    // then
    assertCreated(pizzaObj1);
    assertCreated(pizzaObj2);
    assertCreated(soupObj1);
    assertCreated(soupObj2);
    assertNotNull(objects);
    assertNotNull(objects.getResult());
    assertEquals(4, objects.getResult().size());
    assertNull(afterFirstPizzaObjects.getError());
    assertEquals(1l, afterFirstPizzaObjects.getResult().size());
  }

  @Test
  public void testDataGetWithAdditional() {
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
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run();
    Result<List<WeaviateObject>> objectsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run();
    Result<List<WeaviateObject>> objsAdditionalT = client.data()
      .objectsGetter()
      .withID(objTID)
      .withClassName("Pizza")
      .withAdditional("classification")
      .withAdditional("nearestNeighbors")
      .withVector()
      .run();
    Result<List<WeaviateObject>> objsAdditionalA = client.data()
      .objectsGetter()
      .withID(objAID)
      .withClassName("Soup")
      .withAdditional("classification")
      .withAdditional("nearestNeighbors")
      .withAdditional("interpretation")
      .withVector()
      .run();
    Result<List<WeaviateObject>> objsAdditionalA1 = client.data()
      .objectsGetter().withID(objAID).withClassName("Soup")
      .run();
    Result<List<WeaviateObject>> objsAdditionalA2 = client.data()
      .objectsGetter().withID(objAID).withClassName("Soup")
      .withAdditional("interpretation")
      .run();
    Result<List<WeaviateObject>> objsAdditionalAError = client.data()
      .objectsGetter().withID(objAID).withClassName("Soup")
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
  public void testDataDelete() {
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
    Result<Boolean> deleteObjT = client.data().deleter()
      .withClassName("Pizza")
      .withID(objTID)
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();
    Result<List<WeaviateObject>> objTlist = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run();
    Result<Boolean> deleteObjA = client.data().deleter()
      .withClassName("Soup")
      .withID(objAID)
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();
    Result<List<WeaviateObject>> objAlist = client.data().objectsGetter().withClassName("Soup").withID(objAID).run();
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
    assertNull(objTlist.getResult());
    assertNotNull(deleteObjA);
    assertTrue(deleteObjA.getResult());
    assertNotNull(objAlist);
    assertNull(objAlist.getResult());
  }

  @Test
  public void testDataUpdate() {
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
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();
    Result<Boolean> updateObjectA = client.data().updater()
      .withClassName("Soup")
      .withID(objAID)
      .withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "ChickenSoup");
        put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      }})
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();
    Result<List<WeaviateObject>> updatedObjsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run();
    Result<List<WeaviateObject>> updatedObjsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run();
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
    assertEquals("Used by humans when their inferior genetics are attacked by microscopic organisms.", updatedObjsA.getResult().get(0).getProperties().get(
      "description"));
  }

  @Test
  public void testDataMerge() {
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
    Result<List<WeaviateObject>> mergedObjsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run();
    Result<List<WeaviateObject>> mergeddObjsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run();
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
    assertEquals("Used by humans when their inferior genetics are attacked by microscopic organisms.", mergeddObjsA.getResult().get(0).getProperties().get(
      "description"));
  }

  @Test
  public void testDataValidate() {
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
      .withProperties(propertiesSchemaT)
      .run();
    Result<Boolean> validateObjA = client.data().validator()
      .withClassName("Soup")
      .withID(objAID)
      .withProperties(propertiesSchemaA)
      .run();
    propertiesSchemaT.put("test", "not existing property");
    Result<Boolean> validateObjT1 = client.data().validator()
      .withClassName("Pizza")
      .withID(objTID)
      .withProperties(propertiesSchemaT)
      .run();
    propertiesSchemaA.put("test", "not existing property");
    Result<Boolean> validateObjA1 = client.data().validator()
      .withClassName("Pizza")
      .withID(objTID)
      .withProperties(propertiesSchemaT)
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
  public void testDataGetWithAdditionalError() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
    Map<String, Object> propertiesSchemaA = new HashMap<>();
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
      .objectsGetter()
      .withID(objTID)
      .withClassName("Pizza")
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

  @Test
  public void testDataCreateWithArrayType() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
      .className("ClassArrays")
      .description("Class which properties are all array properties")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .properties(new ArrayList() {{
        add(Property.builder()
          .dataType(new ArrayList() {{
            add(DataType.TEXT_ARRAY);
          }})
          .name("stringArray")
          .build());
        add(Property.builder()
          .dataType(new ArrayList() {{
            add(DataType.TEXT_ARRAY);
          }})
          .name("textArray")
          .build());
        add(Property.builder()
          .dataType(new ArrayList() {{
            add(DataType.INT_ARRAY);
          }})
          .name("intArray")
          .build());
        add(Property.builder()
          .dataType(new ArrayList() {{
            add(DataType.NUMBER_ARRAY);
          }})
          .name("numberArray")
          .build());
        add(Property.builder()
          .dataType(new ArrayList() {{
            add(DataType.BOOLEAN_ARRAY);
          }})
          .name("booleanArray")
          .build());
      }})
      .build();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("stringArray", new String[]{"a", "b"});
    propertiesSchemaT.put("textArray", new String[]{"c", "d"});
    propertiesSchemaT.put("intArray", new Integer[]{1, 2});
    propertiesSchemaT.put("numberArray", new Float[]{3.3f, 4.4f});
    propertiesSchemaT.put("booleanArray", new Boolean[]{true, false});
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<WeaviateObject> objectT = client.data().creator()
      .withClassName("ClassArrays")
      .withID(objTID)
      .withProperties(propertiesSchemaT)
      .run();
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("ClassArrays").withID(objTID).run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertEquals(1, schemaAfterCreate.getResult().getClasses().size());
    // data check
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectsT);
    assertNotNull(objectsT.getResult());
    assertEquals(1, objectsT.getResult().size());
    assertEquals(objTID, objectsT.getResult().get(0).getId());
    assertNotNull(objectsT.getResult().get(0).getProperties());
    assertEquals(5, objectsT.getResult().get(0).getProperties().size());
    assertEquals("ClassArrays", objectsT.getResult().get(0).getClassName());
    checkArrays(objectsT.getResult().get(0).getProperties().get("stringArray"), 2, "a", "b");
    checkArrays(objectsT.getResult().get(0).getProperties().get("textArray"), 2, "c", "d");
    checkArrays(objectsT.getResult().get(0).getProperties().get("intArray"), 2, 1.0, 2.0);
    checkArrays(objectsT.getResult().get(0).getProperties().get("numberArray"), 2, 3.3, 4.4);
    checkArrays(objectsT.getResult().get(0).getProperties().get("booleanArray"), 2, true, false);
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testDataGetWithVector() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
      .className("ClassCustomVector")
      .description("Class with custom vector")
      .vectorizer("none")
      .properties(new ArrayList() {{
        add(Property.builder()
          .dataType(new ArrayList() {{
            add(DataType.TEXT);
          }})
          .name("foo")
          .build());
      }})
      .build();
    String objTID = "addfd256-8574-442b-9293-9205193737ee";
    Map<String, Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("foo", "bar");
    Float[] vectorObjT = new Float[]{-0.26736435f, -0.112380296f, 0.29648793f, 0.39212644f, 0.0033650293f, -0.07112332f, 0.07513781f, 0.22459874f};
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<WeaviateObject> objectT = client.data().creator()
      .withClassName("ClassCustomVector")
      .withID(objTID)
      .withVector(vectorObjT)
      .withProperties(propertiesSchemaT)
      .run();
    Result<List<WeaviateObject>> objT = client.data()
      .objectsGetter()
      .withClassName("ClassCustomVector").withID(objTID)
      .withVector()
      .run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertEquals(1, schemaAfterCreate.getResult().getClasses().size());
    // check the object
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertNotNull(objT);
    assertNull(objT.getError());
    assertNotNull(objT.getResult());
    assertEquals(objT.getResult().size(), 1);
    assertArrayEquals(objT.getResult().get(0).getVector(), vectorObjT);
    // clean up
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testObjectCheck() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    String nonExistentObjectID = "11111111-1111-1111-aaaa-aaaaaaaaaaaa";
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
    // check object existence
    Result<Boolean> checkObjT = client.data().checker().withClassName("Pizza").withID(objTID).run();
    Result<Boolean> checkObjA = client.data().checker().withClassName("Soup").withID(objAID).run();
    Result<List<WeaviateObject>> objA = client.data()
      .objectsGetter()
      .withID(objAID)
      .withClassName("Soup")
      .withVector()
      .run();
    Result<List<WeaviateObject>> objT = client.data()
      .objectsGetter()
      .withID(objTID)
      .withClassName("Pizza")
      .withVector()
      .run();
    Result<Boolean> checkNonexistentObject = client.data().checker().withClassName("Pizza").withID(nonExistentObjectID).run();
    // delete all objects from Weaviate
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    // check object's existence status after clean up
    Result<Boolean> checkObjTAfterDelete = client.data().checker().withClassName("Pizza").withID(objTID).run();
    Result<Boolean> checkObjAAfterDelete = client.data().checker().withClassName("Soup").withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objectT);
    assertNotNull(objectT.getResult());
    assertEquals(objTID, objectT.getResult().getId());
    assertNotNull(objectA);
    assertNotNull(objectA.getResult());
    assertEquals(objAID, objectA.getResult().getId());
    assertNotNull(checkObjT);
    assertTrue(checkObjT.getResult());
    assertNotNull(checkObjA);
    assertTrue(checkObjA.getResult());
    assertNotNull(objA.getResult());
    assertEquals(objA.getResult().size(), 1);
    assertEquals(objA.getResult().get(0).getId(), objAID);
    assertNotNull(objT.getResult());
    assertEquals(objT.getResult().size(), 1);
    assertEquals(objT.getResult().get(0).getId(), objTID);
    assertNotNull(checkNonexistentObject);
    assertFalse(checkNonexistentObject.getResult());
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
    assertNotNull(checkObjTAfterDelete);
    assertFalse(checkObjTAfterDelete.getResult());
    assertNull(checkObjTAfterDelete.getError());
    assertNotNull(checkObjAAfterDelete);
    assertFalse(checkObjAAfterDelete.getResult());
    assertNull(checkObjAAfterDelete.getError());
  }

  @Test
  public void testDataCreateWithIDInNotUUIDFormat() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    String objID = "TODO_4";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "name");
    propertiesSchemaT.put("description", "description");
    // when
    Result<WeaviateObject> objectT = client.data().creator()
      .withID(objID)
      .withClassName("Pizza")
      .withProperties(propertiesSchemaT)
      .run();
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objID).run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(objectT);
    assertNull(objectT.getResult());
    assertNotNull(objectT.getError());
    assertNotNull(objectT.getError().getMessages());
    assertNotNull(objectT.getError().getMessages().get(0));
    assertEquals(422, objectT.getError().getStatusCode());
    assertEquals("id in body must be of type uuid: \"TODO_4\"", objectT.getError().getMessages().get(0).getMessage());
    assertNotNull(objectsT);
    assertNull(objectsT.getResult());
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testDataGetUsingClassParameter() {
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
    Result<List<WeaviateObject>> pizzaObjects = client.data().objectsGetter().withClassName("Pizza").run();
    Result<List<WeaviateObject>> soupObjects = client.data().objectsGetter().withClassName("Soup").run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertCreated(pizzaObj1);
    assertCreated(pizzaObj2);
    assertCreated(soupObj1);
    assertCreated(soupObj2);
    assertNotNull(objects);
    assertNotNull(objects.getResult());
    assertEquals(4, objects.getResult().size());
    assertNotNull(pizzaObjects);
    assertNotNull(pizzaObjects.getResult());
    assertEquals(2, pizzaObjects.getResult().size());
    assertNotNull(soupObjects);
    assertNotNull(soupObjects.getResult());
    assertEquals(2, soupObjects.getResult().size());
  }

  private void assertCreated(Result<WeaviateObject> obj) {
    assertNotNull(obj);
    assertNotNull(obj.getResult());
    assertNotNull(obj.getResult().getId());
  }

  private void checkArrays(Object property, int size, Object... contains) {
    assertNotNull(property);
    assertEquals(ArrayList.class, property.getClass());
    List l = (List) property;
    assertEquals(size, l.size());
    for (Object c : contains) {
      assertTrue(l.contains(c));
    }
  }

  @Test
  public void shouldSupportUUID() {
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
          .build()
      ))
      .build();

    String id = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> properties = new HashMap<>();
    properties.put("uuidProp", "7aaa79d3-a564-45db-8fa8-c49e20b8a39a");
    properties.put("uuidArrayProp", new String[]{
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

    Result<List<WeaviateObject>> objectsStatus = client.data().objectsGetter()
      .withClassName(className)
      .withID(id)
      .run();

    assertThat(objectsStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList()
      .hasSize(1)
      .first().extracting(obj -> ((WeaviateObject) obj).getProperties())
      .returns("7aaa79d3-a564-45db-8fa8-c49e20b8a39a", props -> props.get("uuidProp"))
      .returns(Arrays.asList(
        "f70512a3-26cb-4ae4-9369-204555917f15",
        "9e516f40-fd54-4083-a476-f4675b2b5f92"
      ), props -> props.get("uuidArrayProp"));

    Result<Boolean> deleteStatus = client.schema().allDeleter().run();

    assertThat(deleteStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }
}

