package io.weaviate.integration.client.async.data;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.data.DataTestSuite;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientDataTest {
  private String address;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();
  }

  @Test
  public void testDataCreate() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataCreate.objTID;
    String objAID = DataTestSuite.testDataCreate.objAID;
    Map<String, Object> propertiesSchemaT = DataTestSuite.testDataCreate.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataCreate.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run()
        .get();
      Result<WeaviateObject> objectA = client.data().creator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run()
        .get();
      Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run().get();
      Result<List<WeaviateObject>> objectsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataCreate.assertResults(objectT, objectA, objectsT, objectsA);
    }
  }

  @Test
  public void testDataCreateWithSpecialCharacters() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataCreateWithSpecialCharacters.objTID;
    String name = DataTestSuite.testDataCreateWithSpecialCharacters.name;
    String description = DataTestSuite.testDataCreateWithSpecialCharacters.description;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataCreateWithSpecialCharacters.propertiesSchemaT();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run()
        .get();
      Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataCreateWithSpecialCharacters.assertResults(objectT, objectsT);
    }
  }

  @Test
  public void testDataGetActionsThings() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> pizzaObj1 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, Object>() {{
        put("name", "Margherita");
        put("description", "plain");
      }}).run().get();
      Result<WeaviateObject> pizzaObj2 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "Pepperoni");
        put("description", "meat");
      }}).run().get();
      Result<WeaviateObject> soupObj1 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "Chicken");
        put("description", "plain");
      }}).run().get();
      Result<WeaviateObject> soupObj2 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "Tofu");
        put("description", "vegetarian");
      }}).run().get();
      Result<List<WeaviateObject>> objects = client.data().objectsGetter().run().get();
      Result<List<WeaviateObject>> objects1 = client.data().objectsGetter().withClassName("Pizza").withLimit(1).run().get();
      assertNull(objects1.getError());
      assertEquals(1l, objects1.getResult().size());
      String firstPizzaID = objects1.getResult().get(0).getId();
      Result<List<WeaviateObject>> afterFirstPizzaObjects = client.data()
        .objectsGetter()
        .withClassName("Pizza").withAfter(firstPizzaID).withLimit(1)
        .run().get();

      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataGetActionsThings.assertResults(pizzaObj1, pizzaObj2, soupObj1, soupObj2, objects, afterFirstPizzaObjects);
    }
  }

  @Test
  public void testDataGetWithAdditional() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataGetWithAdditional.objTID;
    String objAID = DataTestSuite.testDataGetWithAdditional.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataGetWithAdditional.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataGetWithAdditional.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<WeaviateObject> objectA = client.data().creator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .run().get();
      Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run().get();
      Result<List<WeaviateObject>> objectsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run().get();
      Result<List<WeaviateObject>> objsAdditionalT = client.data()
        .objectsGetter()
        .withID(objTID)
        .withClassName("Pizza")
        .withAdditional("classification")
        .withAdditional("nearestNeighbors")
        .withVector()
        .run().get();
      Result<List<WeaviateObject>> objsAdditionalA = client.data()
        .objectsGetter()
        .withID(objAID)
        .withClassName("Soup")
        .withAdditional("classification")
        .withAdditional("nearestNeighbors")
        .withAdditional("interpretation")
        .withVector()
        .run().get();
      Result<List<WeaviateObject>> objsAdditionalA1 = client.data()
        .objectsGetter().withID(objAID).withClassName("Soup")
        .run().get();
      Result<List<WeaviateObject>> objsAdditionalA2 = client.data()
        .objectsGetter().withID(objAID).withClassName("Soup")
        .withAdditional("interpretation")
        .run().get();
      Result<List<WeaviateObject>> objsAdditionalAError = client.data()
        .objectsGetter().withID(objAID).withClassName("Soup")
        .withAdditional("featureProjection")
        .run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataGetWithAdditional.assertResults(objectT, objectA, objectsT, objectsA,
        objsAdditionalT, objsAdditionalA, objsAdditionalA1, objsAdditionalA2,
        objsAdditionalAError);
    }
  }

  @Test
  public void testDataDelete() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataDelete.objTID;
    String objAID = DataTestSuite.testDataDelete.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataDelete.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataDelete.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<WeaviateObject> objectA = client.data().creator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .run().get();
      Result<Boolean> deleteObjT = client.data().deleter()
        .withClassName("Pizza")
        .withID(objTID)
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run().get();
      Result<List<WeaviateObject>> objTlist = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run().get();
      Result<Boolean> deleteObjA = client.data().deleter()
        .withClassName("Soup")
        .withID(objAID)
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run().get();
      Result<List<WeaviateObject>> objAlist = client.data().objectsGetter().withClassName("Soup").withID(objAID).run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataDelete.assertResults(objectT, objectA, deleteObjT, objTlist, deleteObjA, objAlist);
    }
  }

  @Test
  public void testDataUpdate() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataUpdate.objTID;
    String objAID = DataTestSuite.testDataUpdate.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataUpdate.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataUpdate.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<WeaviateObject> objectA = client.data().creator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .run().get();
      Result<Boolean> updateObjectT = client.data().updater()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(new HashMap<String, java.lang.Object>() {{
          put("name", "Hawaii");
          put("description", "Universally accepted to be the best pizza ever created.");
        }})
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run().get();
      Result<Boolean> updateObjectA = client.data().updater()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(new HashMap<String, java.lang.Object>() {{
          put("name", "ChickenSoup");
          put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
        }})
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run().get();
      Result<List<WeaviateObject>> updatedObjsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run().get();
      Result<List<WeaviateObject>> updatedObjsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataUpdate.assertResults(objectT, objectA, updateObjectT, updateObjectA, updatedObjsT, updatedObjsA);
    }
  }

  @Test
  public void testDataMerge() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataMerge.objTID;
    String objAID = DataTestSuite.testDataMerge.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataMerge.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataMerge.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<WeaviateObject> objectA = client.data().creator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .run().get();
      Result<Boolean> mergeObjectT = client.data().updater()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(new HashMap<String, java.lang.Object>() {{
          put("description", "Universally accepted to be the best pizza ever created.");
        }})
        .withMerge()
        .run().get();
      Result<Boolean> mergeObjectA = client.data().updater()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(new HashMap<String, java.lang.Object>() {{
          put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
        }})
        .withMerge()
        .run().get();
      Result<List<WeaviateObject>> mergedObjsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID).run().get();
      Result<List<WeaviateObject>> mergeddObjsA = client.data().objectsGetter().withClassName("Soup").withID(objAID).run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataMerge.assertResults(objectT, objectA, mergeObjectT, mergeObjectA, mergedObjsT, mergeddObjsA);
    }
  }

  @Test
  public void testDataValidate() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataValidate.objTID;
    String objAID = DataTestSuite.testDataValidate.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataValidate.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataValidate.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<Boolean> validateObjT = client.data().validator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<Boolean> validateObjA = client.data().validator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .run().get();
      propertiesSchemaT.put("test", "not existing property");
      Result<Boolean> validateObjT1 = client.data().validator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      propertiesSchemaA.put("test", "not existing property");
      Result<Boolean> validateObjA1 = client.data().validator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataValidate.assertResults(validateObjT, validateObjA, validateObjT1, validateObjA1);
    }
  }

  @Test
  public void testDataGetWithAdditionalError() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataGetWithAdditionalError.objTID;
    String objAID = DataTestSuite.testDataGetWithAdditionalError.objAID;
    Map<String, Object> propertiesSchemaT = DataTestSuite.testDataGetWithAdditionalError.propertiesSchemaT();
    Map<String, Object> propertiesSchemaA = DataTestSuite.testDataGetWithAdditionalError.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<WeaviateObject> objectA = client.data().creator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .run().get();
      Result<List<WeaviateObject>> objsAdditionalT = client.data()
        .objectsGetter()
        .withID(objTID)
        .withClassName("Pizza")
        .withAdditional("featureProjection")
        .withVector()
        .run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataGetWithAdditionalError.assertResults(objectT, objectA, objsAdditionalT);
    }
  }

  @Test
  public void testDataCreateWithArrayType() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateClass clazz = DataTestSuite.testDataCreateWithArrayType.clazz;
    String objTID = DataTestSuite.testDataCreateWithArrayType.objTID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataCreateWithArrayType.propertiesSchemaT();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("ClassArrays")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("ClassArrays").withID(objTID).run().get();
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();
      // then
      DataTestSuite.testDataCreateWithArrayType.assertResults(createStatus, schemaAfterCreate, objectT, objectsT, deleteStatus, schemaAfterDelete);
    }
  }

  @Test
  public void testDataGetWithVector() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateClass clazz = DataTestSuite.testDataGetWithVector.clazz;
    String objTID = DataTestSuite.testDataGetWithVector.objTID;
    Map<String, Object> propertiesSchemaT = DataTestSuite.testDataGetWithVector.propertiesSchemaT();
    Float[] vectorObjT = DataTestSuite.testDataGetWithVector.vectorObjT;
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("ClassCustomVector")
        .withID(objTID)
        .withVector(vectorObjT)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<List<WeaviateObject>> objT = client.data()
        .objectsGetter()
        .withClassName("ClassCustomVector").withID(objTID)
        .withVector()
        .run().get();
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();
      // then
      DataTestSuite.testDataGetWithVector.assertResults(createStatus, schemaAfterCreate, objectT, objT, deleteStatus, schemaAfterDelete);
    }
  }

  @Test
  public void testObjectCheck() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testObjectCheck.objTID;
    String objAID = DataTestSuite.testObjectCheck.objAID;
    String nonExistentObjectID = DataTestSuite.testObjectCheck.nonExistentObjectID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testObjectCheck.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testObjectCheck.propertiesSchemaA();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("Pizza")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<WeaviateObject> objectA = client.data().creator()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(propertiesSchemaA)
        .run().get();
      // check object existence
      Result<Boolean> checkObjT = client.data().checker().withClassName("Pizza").withID(objTID).run().get();
      Result<Boolean> checkObjA = client.data().checker().withClassName("Soup").withID(objAID).run().get();
      Result<List<WeaviateObject>> objA = client.data()
        .objectsGetter()
        .withID(objAID)
        .withClassName("Soup")
        .withVector()
        .run().get();
      Result<List<WeaviateObject>> objT = client.data()
        .objectsGetter()
        .withID(objTID)
        .withClassName("Pizza")
        .withVector()
        .run().get();
      Result<Boolean> checkNonexistentObject = client.data().checker().withClassName("Pizza").withID(nonExistentObjectID).run().get();
      // delete all objects from Weaviate
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      // check object's existence status after clean up
      Result<Boolean> checkObjTAfterDelete = client.data().checker().withClassName("Pizza").withID(objTID).run().get();
      Result<Boolean> checkObjAAfterDelete = client.data().checker().withClassName("Soup").withID(objAID).run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testObjectCheck
        .assertResults(objectT, objectA, checkObjT, checkObjA, objA, objT, checkNonexistentObject, deleteStatus, checkObjTAfterDelete, checkObjAAfterDelete);
    }
  }

  @Test
  public void testDataCreateWithIDInNotUUIDFormat() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    String objID = DataTestSuite.testDataCreateWithIDInNotUUIDFormat.objID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataCreateWithIDInNotUUIDFormat.propertiesSchemaT();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      Result<WeaviateObject> objectT = client.data().creator()
        .withID(objID)
        .withClassName("Pizza")
        .withProperties(propertiesSchemaT)
        .run().get();
      Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("Pizza").withID(objID).run().get();
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();
      // then
      DataTestSuite.testDataCreateWithIDInNotUUIDFormat.assertResults(objectT, objectsT, deleteStatus, schemaAfterDelete);
    }
  }

  @Test
  public void testDataGetUsingClassParameter() throws ExecutionException, InterruptedException {
    // given
    Config config = new Config("http", address);
    WeaviateClient syncClient = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    try (WeaviateAsyncClient client = syncClient.async()) {
      // when
      testGenerics.createWeaviateTestSchemaFoodAsync(client);
      Result<WeaviateObject> pizzaObj1 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "Margherita");
        put("description", "plain");
      }}).run().get();
      Result<WeaviateObject> pizzaObj2 = client.data().creator().withClassName("Pizza").withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "Pepperoni");
        put("description", "meat");
      }}).run().get();
      Result<WeaviateObject> soupObj1 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "Chicken");
        put("description", "plain");
      }}).run().get();
      Result<WeaviateObject> soupObj2 = client.data().creator().withClassName("Soup").withProperties(new HashMap<String, java.lang.Object>() {{
        put("name", "Tofu");
        put("description", "vegetarian");
      }}).run().get();
      Result<List<WeaviateObject>> objects = client.data().objectsGetter().run().get();
      Result<List<WeaviateObject>> pizzaObjects = client.data().objectsGetter().withClassName("Pizza").run().get();
      Result<List<WeaviateObject>> soupObjects = client.data().objectsGetter().withClassName("Soup").run().get();
      testGenerics.cleanupWeaviateAsync(client);
      // then
      DataTestSuite.testDataGetUsingClassParameter.assertResults(pizzaObj1, pizzaObj2, soupObj1, soupObj2, objects, pizzaObjects, soupObjects);
    }
  }
}
