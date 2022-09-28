package technology.semi.weaviate.integration.client.deprecated.data;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.ObjectReference;
import technology.semi.weaviate.client.v1.data.model.SingleRef;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClientReferencesDeprecatedAPITest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
          new File("src/test/resources/deprecated-api/docker-compose-deprecated-api-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    address = host + ":" + port;
  }

  private void checkReference(Result<List<WeaviateObject>> result, String refID) {
    assertNotNull(result);
    assertNull(result.getError());
    assertNotNull(result.getResult());
    assertNotNull(result.getResult().get(0));
    assertNotNull(result.getResult().get(0).getProperties());
    assertNotNull(result.getResult().get(0).getProperties().get("otherFoods"));
    assertTrue(result.getResult().get(0).getProperties().get("otherFoods") instanceof List);
    List resultOtherFoods = (List) result.getResult().get(0).getProperties().get("otherFoods");
    if (refID != null) {
      assertTrue(resultOtherFoods.size() > 0);
      assertNotNull(resultOtherFoods.get(0));
      assertTrue(resultOtherFoods.get(0) instanceof Map);
      Map propOtherFoods = (Map) resultOtherFoods.get(0);
      assertEquals(propOtherFoods.get("beacon"), "weaviate://localhost/"+refID);
      assertEquals(propOtherFoods.get("href"), "/v1/objects/"+refID);
    } else {
      assertEquals(resultOtherFoods.size(), 0);
    }
  }

  @Test
  public void testDataCreateWithReferenceCreate() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> propertiesSchemaT = new HashMap() {{
      put("name", "Hawaii");
      put("description", "Universally accepted to be the best pizza ever created.");
    }};
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, Object> propertiesSchemaA = new HashMap() {{
      put("name", "ChickenSoup");
      put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    }};
    // when
    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(client);
    Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run();
    Result<WeaviateObject> objACreate = client.data().creator().withClassName("Soup").withID(objAID).withProperties(propertiesSchemaA).run();
    // Thing -> Action
    // Payload to reference the ChickenSoup
    SingleRef chickenSoupRef = client.data().referencePayloadBuilder().withID(objAID).payload();
    // Add the reference to the ChickenSoup to the Pizza OtherFoods reference
    Result<Boolean> otherFoodsPizzaRefCreate = client.data().referenceCreator()
            .withID(objTID)
            .withReferenceProperty("otherFoods")
            .withReference(chickenSoupRef)
            .run();
    // Action -> Thing
    // Payload to reference the Hawaii
    SingleRef hawaiiRef = client.data().referencePayloadBuilder().withID(objTID).payload();
    // Add the reference to the Hawaii to the Soup OtherFoods reference
    Result<Boolean> otherFoodsSoupRefCreate = client.data().referenceCreator()
            .withID(objAID)
            .withReferenceProperty("otherFoods")
            .withReference(hawaiiRef)
            .run();
    // Get the objects
    Result<List<WeaviateObject>> things = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> actions = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
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
    checkReference(things, objAID);
    // check objA
    checkReference(actions, objTID);
  }

  @Test
  public void testDataCreateWithReferenceReplace() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> propertiesSchemaT = new HashMap() {{
      put("name", "Hawaii");
      put("description", "Universally accepted to be the best pizza ever created.");
    }};
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, Object> propertiesSchemaA = new HashMap() {{
      put("name", "ChickenSoup");
      put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    }};
    SingleRef chickenSoupRef = client.data().referencePayloadBuilder().withID(objAID).payload();
    SingleRef hawaiiRef = client.data().referencePayloadBuilder().withID(objTID).payload();
    // when
    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(client);
    Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run();
    Result<WeaviateObject> objACreate = client.data().creator().withClassName("Soup").withID(objAID).withProperties(propertiesSchemaA).run();
    // Thing -> Action
    // Payload to reference the ChickenSoup
    // Add the reference to the ChickenSoup to the Pizza OtherFoods reference
    Result<Boolean> otherFoodsPizzaRefCreate = client.data().referenceCreator()
            .withID(objTID)
            .withReferenceProperty("otherFoods")
            .withReference(chickenSoupRef)
            .run();
    // Action -> Thing
    // Payload to reference the Hawaii
    // Add the reference to the Hawaii to the Soup OtherFoods reference
    Result<Boolean> otherFoodsSoupRefCreate = client.data().referenceCreator()
            .withID(objAID)
            .withReferenceProperty("otherFoods")
            .withReference(hawaiiRef)
            .run();
    // Get the objects
    Result<List<WeaviateObject>> things = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> actions = client.data().objectsGetter().withID(objAID).run();
    // Replace the above reference with self references
    // Thing -> Thing
    Result<Boolean> otherFoodsPizzaRefReplace = client.data().referenceReplacer()
            .withID(objTID)
            .withReferenceProperty("otherFoods")
            .withReferences(new SingleRef[]{ hawaiiRef })
            .run();
    // Action -> Action
    Result<Boolean> otherFoodsSoupRefReplace = client.data().referenceReplacer()
            .withID(objAID)
            .withReferenceProperty("otherFoods")
            .withReferences(new SingleRef[]{ chickenSoupRef })
            .run();
    Result<List<WeaviateObject>> thingsReplaced = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> actionsReplaced = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
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
    checkReference(things, objAID);
    // check objA
    checkReference(actions, objTID);
    // check objT replaced
    checkReference(thingsReplaced, objTID);
    // check objA replaced
    checkReference(actionsReplaced, objAID);
  }

  @Test
  public void testDataCreateWithReferenceDelete() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> propertiesSchemaT = new HashMap() {{
      put("name", "Hawaii");
      put("description", "Universally accepted to be the best pizza ever created.");
    }};
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, Object> propertiesSchemaA = new HashMap() {{
      put("name", "ChickenSoup");
      put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    }};
    // when
    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(client);
    Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run();
    Result<WeaviateObject> objACreate = client.data().creator().withClassName("Soup").withID(objAID).withProperties(propertiesSchemaA).run();
    // Thing -> Action
    // Payload to reference the ChickenSoup
    SingleRef chickenSoupRef = client.data().referencePayloadBuilder().withID(objAID).payload();
    // Add the reference to the ChickenSoup to the Pizza OtherFoods reference
    Result<Boolean> otherFoodsPizzaRefCreate = client.data().referenceCreator()
            .withID(objTID)
            .withReferenceProperty("otherFoods")
            .withReference(chickenSoupRef)
            .run();
    // Action -> Thing
    // Payload to reference the Hawaii
    SingleRef hawaiiRef = client.data().referencePayloadBuilder().withID(objTID).payload();
    // Add the reference to the Hawaii to the Soup OtherFoods reference
    Result<Boolean> otherFoodsSoupRefCreate = client.data().referenceCreator()
            .withID(objAID)
            .withReferenceProperty("otherFoods")
            .withReference(hawaiiRef)
            .run();
    // Get the objects
    Result<List<WeaviateObject>> things = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> actions = client.data().objectsGetter().withID(objAID).run();
    // Delete ref
    Result<Boolean> otherFoodsPizzaRefDelete = client.data().referenceDeleter()
            .withID(objTID)
            .withReferenceProperty("otherFoods")
            .withReference(chickenSoupRef).run();
    Result<Boolean> otherFoodsSoupRefDelete = client.data().referenceDeleter()
            .withID(objAID)
            .withReferenceProperty("otherFoods")
            .withReference(hawaiiRef).run();
    // Get the objects
    Result<List<WeaviateObject>> thingsAfterRefDelete = client.data().objectsGetter().withID(objTID).run();
    Result<List<WeaviateObject>> actionsAfterRefDelete = client.data().objectsGetter().withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
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
    checkReference(things, objAID);
    // check objA
    checkReference(actions, objTID);
    // check ref delete
    assertNotNull(otherFoodsPizzaRefDelete);
    assertNull(otherFoodsPizzaRefDelete.getError());
    assertTrue(otherFoodsPizzaRefDelete.getResult());
    assertNotNull(otherFoodsSoupRefDelete);
    assertNull(otherFoodsSoupRefDelete.getError());
    assertTrue(otherFoodsSoupRefDelete.getResult());
    // check objT after delete, should be null
    checkReference(thingsAfterRefDelete, null);
    // check objA after delete, should be null
    checkReference(actionsAfterRefDelete, null);
  }

  @Test
  public void testDataCreateWithAddReferenceUsingProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, Object> propertiesSchemaT = new HashMap() {{
      put("name", "Hawaii");
      put("description", "Universally accepted to be the best pizza ever created.");
    }};
    String objRefBeaconID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba92";
    Map<String, Object> propertiesSchemaRefBeacon = new HashMap() {{
      put("name", "RefBeaconSoup");
      put("description", "Used only to check if reference can be added.");
      put("otherFoods", new ObjectReference[]{
              ObjectReference.builder().beacon("weaviate://localhost/abefd256-8574-442b-9293-9205193737ee").build()
      });
    }};
    // when
    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(client);
    Result<WeaviateObject> objTCreate = client.data().creator().withClassName("Pizza").withID(objTID).withProperties(propertiesSchemaT).run();
    // create object with a reference to objT
    Result<WeaviateObject> objRefBeaconCreate = client.data().creator()
            .withClassName("Soup")
            .withID(objRefBeaconID)
            .withProperties(propertiesSchemaRefBeacon)
            .run();
    // Get the object reference beacon to check if otherFoods reference has been set
    Result<List<WeaviateObject>> objRefBeaconGet = client.data().objectsGetter().withID(objRefBeaconID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objTCreate);
    assertNull(objTCreate.getError());
    assertNotNull(objRefBeaconCreate);
    assertNull(objRefBeaconCreate.getError());
    // check objT
    checkReference(objRefBeaconGet, objTID);
  }
}
