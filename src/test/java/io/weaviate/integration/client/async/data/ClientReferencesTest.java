package io.weaviate.integration.client.async.data;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.data.model.ObjectReference;
import io.weaviate.client.v1.data.model.SingleRef;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientReferencesTest {
  private String address;
  private WeaviateClient syncClient;
  
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();

    Config config = new Config("http", address);
    syncClient = new WeaviateClient(config);

    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(syncClient);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(syncClient);
  }

  @Test
  public void testDataCreateWithReferenceCreate() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      String objTID = "abefd256-8574-442b-9293-9205193737ee";
      Map<String, Object> propertiesSchemaT = new HashMap<String, Object>() {{
        put("name", "Hawaii");
        put("description", "Universally accepted to be the best pizza ever created.");
      }};
      String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
      Map<String, Object> propertiesSchemaA = new HashMap<String, Object>() {{
        put("name", "ChickenSoup");
        put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      }};
      // when
      Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run().get();
      Result<WeaviateObject> objACreate = client.data().creator().withClassName("Soup").withID(objAID).withProperties(propertiesSchemaA).run().get();
      // Thing -> Action
      // Payload to reference the ChickenSoup
      SingleRef chickenSoupRef = client.data().referencePayloadBuilder().withID(objAID).withClassName("Soup").payload();
      // Add the reference to the ChickenSoup to the Pizza OtherFoods reference
      Result<Boolean> otherFoodsPizzaRefCreate = client.data().referenceCreator()
              .withID(objTID)
              .withClassName("Pizza")
              .withReferenceProperty("otherFoods")
              .withReference(chickenSoupRef)
              .withConsistencyLevel(ConsistencyLevel.QUORUM)
              .run().get();
      // Action -> Thing
      // Payload to reference the Hawaii
      SingleRef hawaiiRef = client.data().referencePayloadBuilder().withID(objTID).withClassName("Pizza").payload();
      // Add the reference to the Hawaii to the Soup OtherFoods reference
      Result<Boolean> otherFoodsSoupRefCreate = client.data().referenceCreator()
              .withID(objAID)
              .withClassName("Soup")
              .withReferenceProperty("otherFoods")
              .withReference(hawaiiRef)
              .withConsistencyLevel(ConsistencyLevel.QUORUM)
              .run().get();
      // Get the objects
      Result<List<WeaviateObject>> things = client.data().objectsGetter().withID(objTID).withClassName("Pizza").run().get();
      Result<List<WeaviateObject>> actions = client.data().objectsGetter().withID(objAID).withClassName("Soup").run().get();
      // then
      assertNotNull(objTCreate);
      assertNull(objTCreate.getError());
      assertNotNull(objACreate);
      assertNull(objACreate.getError());
      assertNotNull(otherFoodsPizzaRefCreate);
      assertNull(otherFoodsPizzaRefCreate.getError());
      assertTrue(otherFoodsPizzaRefCreate.getResult());
      assertNotNull(otherFoodsSoupRefCreate);
      assertNull(otherFoodsSoupRefCreate.getError());
      assertTrue(otherFoodsSoupRefCreate.getResult());
      // check objT
      checkReference(things, "Soup", objAID);
      // check objA
      checkReference(actions, "Pizza", objTID);
    }
  }

  @Test
  public void testDataCreateWithReferenceReplace() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      String objTID = "abefd256-8574-442b-9293-9205193737ee";
      Map<String, Object> propertiesSchemaT = new HashMap<String, Object>() {{
        put("name", "Hawaii");
        put("description", "Universally accepted to be the best pizza ever created.");
      }};
      String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
      Map<String, Object> propertiesSchemaA = new HashMap<String, Object>() {{
        put("name", "ChickenSoup");
        put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      }};
      SingleRef chickenSoupRef = client.data().referencePayloadBuilder().withID(objAID).withClassName("Soup").payload();
      SingleRef hawaiiRef = client.data().referencePayloadBuilder().withID(objTID).withClassName("Pizza").payload();
      // when
      Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run().get();
      Result<WeaviateObject> objACreate = client.data().creator().withClassName("Soup").withID(objAID).withProperties(propertiesSchemaA).run().get();
      // Thing -> Action
      // Payload to reference the ChickenSoup
      // Add the reference to the ChickenSoup to the Pizza OtherFoods reference
      Result<Boolean> otherFoodsPizzaRefCreate = client.data().referenceCreator()
              .withID(objTID)
              .withClassName("Pizza")
              .withReferenceProperty("otherFoods")
              .withReference(chickenSoupRef)
              .run().get();
      // Action -> Thing
      // Payload to reference the Hawaii
      // Add the reference to the Hawaii to the Soup OtherFoods reference
      Result<Boolean> otherFoodsSoupRefCreate = client.data().referenceCreator()
              .withID(objAID)
              .withClassName("Soup")
              .withReferenceProperty("otherFoods")
              .withReference(hawaiiRef)
              .run().get();
      // Get the objects
      Result<List<WeaviateObject>> things = client.data().objectsGetter().withID(objTID).withClassName("Pizza").run().get();
      Result<List<WeaviateObject>> actions = client.data().objectsGetter().withID(objAID).withClassName("Soup").run().get();
      // Replace the above reference with self references
      // Thing -> Thing
      client.data().referenceReplacer()
              .withID(objTID)
              .withClassName("Pizza")
              .withReferenceProperty("otherFoods")
              .withReferences(new SingleRef[]{ hawaiiRef })
              .withConsistencyLevel(ConsistencyLevel.QUORUM)
              .run().get();
      // Action -> Action
      client.data().referenceReplacer()
              .withID(objAID)
              .withClassName("Soup")
              .withReferenceProperty("otherFoods")
              .withReferences(new SingleRef[]{ chickenSoupRef })
              .withConsistencyLevel(ConsistencyLevel.QUORUM)
              .run().get();
      Result<List<WeaviateObject>> thingsReplaced = client.data().objectsGetter().withID(objTID).withClassName("Pizza").run().get();
      Result<List<WeaviateObject>> actionsReplaced = client.data().objectsGetter().withID(objAID).withClassName("Soup").run().get();
      // then
      assertNotNull(objTCreate);
      assertNull(objTCreate.getError());
      assertNotNull(objACreate);
      assertNull(objACreate.getError());
      assertNotNull(otherFoodsPizzaRefCreate);
      assertNull(otherFoodsPizzaRefCreate.getError());
      assertTrue(otherFoodsPizzaRefCreate.getResult());
      assertNotNull(otherFoodsSoupRefCreate);
      assertNull(otherFoodsSoupRefCreate.getError());
      assertTrue(otherFoodsSoupRefCreate.getResult());
      // check objT
      checkReference(things, "Soup", objAID);
      // check objA
      checkReference(actions, "Pizza", objTID);
      // check objT replaced
      checkReference(thingsReplaced, "Pizza", objTID);
      // check objA replaced
      checkReference(actionsReplaced, "Soup", objAID);
    }
  }

  @Test
  public void testDataCreateWithReferenceDelete() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      String objTID = "abefd256-8574-442b-9293-9205193737ee";
      Map<String, Object> propertiesSchemaT = new HashMap<String, Object>() {{
        put("name", "Hawaii");
        put("description", "Universally accepted to be the best pizza ever created.");
      }};
      String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
      Map<String, Object> propertiesSchemaA = new HashMap<String, Object>() {{
        put("name", "ChickenSoup");
        put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      }};
      // when
      Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run().get();
      Result<WeaviateObject> objACreate = client.data().creator().withClassName("Soup").withID(objAID).withProperties(propertiesSchemaA).run().get();
      // Thing -> Action
      // Payload to reference the ChickenSoup
      SingleRef chickenSoupRef = client.data().referencePayloadBuilder().withID(objAID).withClassName("Soup").payload();
      // Add the reference to the ChickenSoup to the Pizza OtherFoods reference
      Result<Boolean> otherFoodsPizzaRefCreate = client.data().referenceCreator()
              .withID(objTID)
              .withClassName("Pizza")
              .withReferenceProperty("otherFoods")
              .withReference(chickenSoupRef)
              .run().get();
      // Action -> Thing
      // Payload to reference the Hawaii
      SingleRef hawaiiRef = client.data().referencePayloadBuilder().withID(objTID).withClassName("Pizza").payload();
      // Add the reference to the Hawaii to the Soup OtherFoods reference
      Result<Boolean> otherFoodsSoupRefCreate = client.data().referenceCreator()
              .withID(objAID)
              .withClassName("Soup")
              .withReferenceProperty("otherFoods")
              .withReference(hawaiiRef)
              .run().get();
      // Get the objects
      Result<List<WeaviateObject>> things = client.data().objectsGetter().withID(objTID).withClassName("Pizza").run().get();
      Result<List<WeaviateObject>> actions = client.data().objectsGetter().withID(objAID).withClassName("Soup").run().get();
      // Delete ref
      Result<Boolean> otherFoodsPizzaRefDelete = client.data().referenceDeleter()
              .withID(objTID)
              .withClassName("Pizza")
              .withReferenceProperty("otherFoods")
              .withReference(chickenSoupRef)
              .withConsistencyLevel(ConsistencyLevel.QUORUM)
              .run().get();
      Result<Boolean> otherFoodsSoupRefDelete = client.data().referenceDeleter()
              .withID(objAID)
              .withClassName("Soup")
              .withReferenceProperty("otherFoods")
              .withReference(hawaiiRef)
              .withConsistencyLevel(ConsistencyLevel.QUORUM)
              .run().get();
      // Get the objects
      Result<List<WeaviateObject>> thingsAfterRefDelete = client.data().objectsGetter().withID(objTID).withClassName("Pizza").run().get();
      Result<List<WeaviateObject>> actionsAfterRefDelete = client.data().objectsGetter().withID(objAID).withClassName("Soup").run().get();
      // then
      assertNotNull(objTCreate);
      assertNull(objTCreate.getError());
      assertNotNull(objACreate);
      assertNull(objACreate.getError());
      assertNotNull(otherFoodsPizzaRefCreate);
      assertNull(otherFoodsPizzaRefCreate.getError());
      assertTrue(otherFoodsPizzaRefCreate.getResult());
      assertNotNull(otherFoodsSoupRefCreate);
      assertNull(otherFoodsSoupRefCreate.getError());
      assertTrue(otherFoodsSoupRefCreate.getResult());
      // check objT
      checkReference(things, "Soup", objAID);
      // check objA
      checkReference(actions, "Pizza", objTID);
      // check ref delete
      assertNotNull(otherFoodsPizzaRefDelete);
      assertNull(otherFoodsPizzaRefDelete.getError());
      assertTrue(otherFoodsPizzaRefDelete.getResult());
      assertNotNull(otherFoodsSoupRefDelete);
      assertNull(otherFoodsSoupRefDelete.getError());
      assertTrue(otherFoodsSoupRefDelete.getResult());
      // check objT after delete, should be null
      checkReference(thingsAfterRefDelete, null, null);
      // check objA after delete, should be null
      checkReference(actionsAfterRefDelete, null, null);
    }
  }

  @Test
  public void testDataCreateWithAddReferenceUsingProperties() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      String objTID = "abefd256-8574-442b-9293-9205193737ee";
      Map<String, Object> propertiesSchemaT = new HashMap<String, Object>() {{
        put("name", "Hawaii");
        put("description", "Universally accepted to be the best pizza ever created.");
      }};
      String objRefBeaconID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba92";
      Map<String, Object> propertiesSchemaRefBeacon = new HashMap<String, Object>() {{
        put("name", "RefBeaconSoup");
        put("description", "Used only to check if reference can be added.");
        put("otherFoods", new ObjectReference[]{
                ObjectReference.builder().beacon("weaviate://localhost/Pizza/abefd256-8574-442b-9293-9205193737ee").build()
        });
      }};
      // when
      Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run().get();
      // create object with a reference to objT
      Result<WeaviateObject> objRefBeaconCreate = client.data().creator()
              .withClassName("Soup")
              .withID(objRefBeaconID)
              .withProperties(propertiesSchemaRefBeacon)
              .run().get();
      // Get the object reference beacon to check if otherFoods reference has been set
      Result<List<WeaviateObject>> objRefBeaconGet = client.data().objectsGetter().withID(objRefBeaconID).withClassName("Soup").run().get();
      // then
      assertNotNull(objTCreate);
      assertNull(objTCreate.getError());
      assertNotNull(objRefBeaconCreate);
      assertNull(objRefBeaconCreate.getError());
      // check objT
      checkReference(objRefBeaconGet, "Pizza", objTID);
    }
  }

  @SuppressWarnings("unchecked")
  private void checkReference(Result<List<WeaviateObject>> result, String className, String refID) {
    assertNotNull(result);
    assertNull(result.getError());
    assertNotNull(result.getResult());
    assertNotNull(result.getResult().get(0));
    assertNotNull(result.getResult().get(0).getProperties());
    assertNotNull(result.getResult().get(0).getProperties().get("otherFoods"));
    assertTrue(result.getResult().get(0).getProperties().get("otherFoods") instanceof List);
    List<Map<String, String>> resultOtherFoods = (List<Map<String, String>>) result.getResult().get(0).getProperties().get("otherFoods");
    if (refID != null) {
      assertTrue(resultOtherFoods.size() > 0);
      assertNotNull(resultOtherFoods.get(0));
      Map<String, String> propOtherFoods = resultOtherFoods.get(0);
      assertEquals(propOtherFoods.get("beacon"), "weaviate://localhost/"+className+"/"+refID);
      assertEquals(propOtherFoods.get("href"), "/v1/objects/"+className+"/"+refID);
    } else {
      assertEquals(resultOtherFoods.size(), 0);
    }
  }
}
