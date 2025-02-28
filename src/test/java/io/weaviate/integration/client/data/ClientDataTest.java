package io.weaviate.integration.client.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.client.v1.misc.model.MultiVectorConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.data.DataTestSuite;

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
    String objTID = DataTestSuite.testDataCreate.objTID;
    String objAID = DataTestSuite.testDataCreate.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataCreate.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataCreate.propertiesSchemaA();
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
    DataTestSuite.testDataCreate.assertResults(objectT, objectA, objectsT, objectsA);
  }

  @Test
  public void testDataCreateAndRetrieveMultiVectors() {
    WeaviateClient client = new WeaviateClient(new Config("http", address));

    // Arrange: Configure collection and create it
    String className = "NamedMultiVectors";
    WeaviateClass weaviateClass = WeaviateClass.builder()
        .className(className)
        .properties(Arrays.asList(
            Property.builder()
                .name("name")
                .dataType(Collections.singletonList(DataType.TEXT))
                .build()))
        .vectorConfig(new HashMap<String, WeaviateClass.VectorConfig>() {
          {
            this.put("regular", WeaviateClass.VectorConfig.builder()
                .vectorizer(new HashMap<String, Object>() {
                  {
                    this.put("none", new Object());
                  }
                })
                .vectorIndexType("hnsw")
                .build());
            this.put("colbert", WeaviateClass.VectorConfig.builder()
                .vectorizer(new HashMap<String, Object>() {
                  {
                    this.put("none", new Object());
                  }
                })
                .vectorIndexConfig(VectorIndexConfig.builder()
                    .multiVector(MultiVectorConfig.builder().build())
                    .build())
                .vectorIndexType("hnsw")
                .build());
          }
        })
        .build();

    Result<Boolean> createResult = client.schema().classCreator().withClass(weaviateClass).run();
    assumeTrue(createResult.getResult(), "schema created successfully");

    String id = UUID.randomUUID().toString();
    Float[][] colbertVector = new Float[][] {
        { 0.11f, 0.22f, 0.33f, 0.123f, -0.900009f, -0.0000000001f },
        { 0.11f, 0.22f, 0.33f, 0.123f, -0.900009f, -0.0000000001f },
    };

    // Act: Insert test data
    Result<WeaviateObject> insertResult = client.data().creator()
        .withID(id).withClassName(className)
        .withProperties(new HashMap<String, Object>() {
          {
            this.put("name", "TestObject-1");
            this.put("title", "The Lord of the Rings");
          }
        })
        .withVectors(new HashMap<String, Float[]>() {
          {
            this.put("regular", colbertVector[0]);
          }
        })
        .withMultiVectors(new HashMap<String, Float[][]>() {
          {
            this.put("colbert", colbertVector);
          }
        })
        .run();

    // Assert: Retrieve object and check its dimensions
    Result<List<WeaviateObject>> getResult = client.data().objectsGetter()
        .withClassName(className).withID(id).withVector().run();

    assertThat(getResult).isNotNull()
        .returns(null, Result::getError).as("get object error")
        .extracting(Result::getResult).isNotNull().as("result not null")
        .extracting(r -> r.get(0)).isNotNull().as("first object")
        .satisfies(o -> {
          assertThat(o.getVectors()).as("1d-vectors")
              .isNotEmpty().containsOnlyKeys("regular");

          assertThat(o.getMultiVectors()).as("multi-vectors")
              .isNotEmpty().containsOnlyKeys("colbert")
              .satisfies(multi -> {
                assertThat(multi.get("colbert")).as("colbert multivector")
                    .isEqualTo(colbertVector);
              });
        }).as("expected object metadata");
  }

  @Test
  public void testDataCreateWithSpecialCharacters() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataCreateWithSpecialCharacters.objTID;
    String name = DataTestSuite.testDataCreateWithSpecialCharacters.name;
    String description = DataTestSuite.testDataCreateWithSpecialCharacters.description;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataCreateWithSpecialCharacters
        .propertiesSchemaT();
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
    DataTestSuite.testDataCreateWithSpecialCharacters.assertResults(objectT, objectsT);
  }

  @Test
  public void testDataGetActionsThings() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Result<WeaviateObject> pizzaObj1 = client.data().creator().withClassName("Pizza")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Margherita");
            put("description", "plain");
          }
        }).run();
    Result<WeaviateObject> pizzaObj2 = client.data().creator().withClassName("Pizza")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Pepperoni");
            put("description", "meat");
          }
        }).run();
    Result<WeaviateObject> soupObj1 = client.data().creator().withClassName("Soup")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Chicken");
            put("description", "plain");
          }
        }).run();
    Result<WeaviateObject> soupObj2 = client.data().creator().withClassName("Soup")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Tofu");
            put("description", "vegetarian");
          }
        }).run();
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
    DataTestSuite.testDataGetActionsThings.assertResults(pizzaObj1, pizzaObj2, soupObj1, soupObj2, objects,
        afterFirstPizzaObjects);
  }

  @Test
  public void testDataGetWithAdditional() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataGetWithAdditional.objTID;
    String objAID = DataTestSuite.testDataGetWithAdditional.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataGetWithAdditional.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataGetWithAdditional.propertiesSchemaA();
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
    DataTestSuite.testDataGetWithAdditional.assertResults(objectT, objectA, objectsT, objectsA,
        objsAdditionalT, objsAdditionalA, objsAdditionalA1, objsAdditionalA2,
        objsAdditionalAError);
  }

  @Test
  public void testDataDelete() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataDelete.objTID;
    String objAID = DataTestSuite.testDataDelete.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataDelete.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataDelete.propertiesSchemaA();
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
    DataTestSuite.testDataDelete.assertResults(objectT, objectA, deleteObjT, objTlist, deleteObjA, objAlist);
  }

  @Test
  public void testDataUpdate() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataUpdate.objTID;
    String objAID = DataTestSuite.testDataUpdate.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataUpdate.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataUpdate.propertiesSchemaA();
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
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Hawaii");
            put("description", "Universally accepted to be the best pizza ever created.");
          }
        })
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run();
    Result<Boolean> updateObjectA = client.data().updater()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "ChickenSoup");
            put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
          }
        })
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run();
    Result<List<WeaviateObject>> updatedObjsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID)
        .run();
    Result<List<WeaviateObject>> updatedObjsA = client.data().objectsGetter().withClassName("Soup").withID(objAID)
        .run();
    testGenerics.cleanupWeaviate(client);
    // then
    DataTestSuite.testDataUpdate.assertResults(objectT, objectA, updateObjectT, updateObjectA, updatedObjsT,
        updatedObjsA);
  }

  @Test
  public void testDataMerge() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataMerge.objTID;
    String objAID = DataTestSuite.testDataMerge.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataMerge.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataMerge.propertiesSchemaA();
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
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("description", "Universally accepted to be the best pizza ever created.");
          }
        })
        .withMerge()
        .run();
    Result<Boolean> mergeObjectA = client.data().updater()
        .withClassName("Soup")
        .withID(objAID)
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
          }
        })
        .withMerge()
        .run();
    Result<List<WeaviateObject>> mergedObjsT = client.data().objectsGetter().withClassName("Pizza").withID(objTID)
        .run();
    Result<List<WeaviateObject>> mergeddObjsA = client.data().objectsGetter().withClassName("Soup").withID(objAID)
        .run();
    testGenerics.cleanupWeaviate(client);
    // then
    DataTestSuite.testDataMerge.assertResults(objectT, objectA, mergeObjectT, mergeObjectA, mergedObjsT, mergeddObjsA);
  }

  @Test
  public void testDataValidate() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataValidate.objTID;
    String objAID = DataTestSuite.testDataValidate.objAID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataValidate.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testDataValidate.propertiesSchemaA();
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
    DataTestSuite.testDataValidate.assertResults(validateObjT, validateObjA, validateObjT1, validateObjA1);
  }

  @Test
  public void testDataGetWithAdditionalError() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testDataGetWithAdditionalError.objTID;
    String objAID = DataTestSuite.testDataGetWithAdditionalError.objAID;
    Map<String, Object> propertiesSchemaT = DataTestSuite.testDataGetWithAdditionalError.propertiesSchemaT();
    Map<String, Object> propertiesSchemaA = DataTestSuite.testDataGetWithAdditionalError.propertiesSchemaA();
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
    DataTestSuite.testDataGetWithAdditionalError.assertResults(objectT, objectA, objsAdditionalT);
  }

  @Test
  public void testDataCreateWithArrayType() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = DataTestSuite.testDataCreateWithArrayType.clazz;
    String objTID = DataTestSuite.testDataCreateWithArrayType.objTID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataCreateWithArrayType.propertiesSchemaT();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<WeaviateObject> objectT = client.data().creator()
        .withClassName("ClassArrays")
        .withID(objTID)
        .withProperties(propertiesSchemaT)
        .run();
    Result<List<WeaviateObject>> objectsT = client.data().objectsGetter().withClassName("ClassArrays").withID(objTID)
        .run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    DataTestSuite.testDataCreateWithArrayType.assertResults(createStatus, schemaAfterCreate, objectT, objectsT,
        deleteStatus, schemaAfterDelete);
  }

  @Test
  public void testDataGetWithVector() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = DataTestSuite.testDataGetWithVector.clazz;
    String objTID = DataTestSuite.testDataGetWithVector.objTID;
    Map<String, Object> propertiesSchemaT = DataTestSuite.testDataGetWithVector.propertiesSchemaT();
    Float[] vectorObjT = DataTestSuite.testDataGetWithVector.vectorObjT;
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
    DataTestSuite.testDataGetWithVector.assertResults(createStatus, schemaAfterCreate, objectT, objT, deleteStatus,
        schemaAfterDelete);
  }

  @Test
  public void testObjectCheck() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String objTID = DataTestSuite.testObjectCheck.objTID;
    String objAID = DataTestSuite.testObjectCheck.objAID;
    String nonExistentObjectID = DataTestSuite.testObjectCheck.nonExistentObjectID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testObjectCheck.propertiesSchemaT();
    Map<String, java.lang.Object> propertiesSchemaA = DataTestSuite.testObjectCheck.propertiesSchemaA();
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
    Result<Boolean> checkNonexistentObject = client.data().checker().withClassName("Pizza").withID(nonExistentObjectID)
        .run();
    // delete all objects from Weaviate
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    // check object's existence status after clean up
    Result<Boolean> checkObjTAfterDelete = client.data().checker().withClassName("Pizza").withID(objTID).run();
    Result<Boolean> checkObjAAfterDelete = client.data().checker().withClassName("Soup").withID(objAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    DataTestSuite.testObjectCheck
        .assertResults(objectT, objectA, checkObjT, checkObjA, objA, objT, checkNonexistentObject, deleteStatus,
            checkObjTAfterDelete, checkObjAAfterDelete);
  }

  @Test
  public void testDataCreateWithIDInNotUUIDFormat() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    String objID = DataTestSuite.testDataCreateWithIDInNotUUIDFormat.objID;
    Map<String, java.lang.Object> propertiesSchemaT = DataTestSuite.testDataCreateWithIDInNotUUIDFormat
        .propertiesSchemaT();
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
    DataTestSuite.testDataCreateWithIDInNotUUIDFormat.assertResults(objectT, objectsT, deleteStatus, schemaAfterDelete);
  }

  @Test
  public void testDataGetUsingClassParameter() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Result<WeaviateObject> pizzaObj1 = client.data().creator().withClassName("Pizza")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Margherita");
            put("description", "plain");
          }
        }).run();
    Result<WeaviateObject> pizzaObj2 = client.data().creator().withClassName("Pizza")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Pepperoni");
            put("description", "meat");
          }
        }).run();
    Result<WeaviateObject> soupObj1 = client.data().creator().withClassName("Soup")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Chicken");
            put("description", "plain");
          }
        }).run();
    Result<WeaviateObject> soupObj2 = client.data().creator().withClassName("Soup")
        .withProperties(new HashMap<String, java.lang.Object>() {
          {
            put("name", "Tofu");
            put("description", "vegetarian");
          }
        }).run();
    Result<List<WeaviateObject>> objects = client.data().objectsGetter().run();
    Result<List<WeaviateObject>> pizzaObjects = client.data().objectsGetter().withClassName("Pizza").run();
    Result<List<WeaviateObject>> soupObjects = client.data().objectsGetter().withClassName("Soup").run();
    testGenerics.cleanupWeaviate(client);
    // then
    DataTestSuite.testDataGetUsingClassParameter.assertResults(pizzaObj1, pizzaObj2, soupObj1, soupObj2, objects,
        pizzaObjects, soupObjects);
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
            "9e516f40-fd54-4083-a476-f4675b2b5f92"), props -> props.get("uuidArrayProp"));

    Result<Boolean> deleteStatus = client.schema().allDeleter().run();

    assertThat(deleteStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);
  }
}
