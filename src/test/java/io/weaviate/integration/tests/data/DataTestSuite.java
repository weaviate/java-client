package io.weaviate.integration.tests.data;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class DataTestSuite {
  public static class testDataCreate {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Hawaii");
      propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
      return propertiesSchemaT;
    }
    public static Map<String, java.lang.Object> propertiesSchemaA() {
      Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "ChickenSoup");
      propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<WeaviateObject> objectA, Result<List<WeaviateObject>> objectsT, Result<List<WeaviateObject>> objectsA) {
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
  }

  public static class testDataCreateWithSpecialCharacters {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String name = "Zażółć gęślą jaźń";
    public static String description = "test äüëö";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", name);
      propertiesSchemaT.put("description", description);
      return propertiesSchemaT;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<List<WeaviateObject>> objectsT) {
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
  }

  public static class testDataGetActionsThings {
    public static void assertResults(Result<WeaviateObject> pizzaObj1, Result<WeaviateObject> pizzaObj2, Result<WeaviateObject> soupObj1,
      Result<WeaviateObject> soupObj2, Result<List<WeaviateObject>> objects, Result<List<WeaviateObject>> afterFirstPizzaObjects) {
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
  }

  public static class testDataGetWithAdditional {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Hawaii");
      propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
      return propertiesSchemaT;
    }
    public static Map<String, java.lang.Object> propertiesSchemaA() {
      Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "ChickenSoup");
      propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<WeaviateObject> objectA, Result<List<WeaviateObject>> objectsT, Result<List<WeaviateObject>> objectsA,
      Result<List<WeaviateObject>> objsAdditionalT, Result<List<WeaviateObject>> objsAdditionalA, Result<List<WeaviateObject>> objsAdditionalA1, Result<List<WeaviateObject>> objsAdditionalA2,
      Result<List<WeaviateObject>> objsAdditionalAError) {
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
  }

  public static class testDataDelete {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Hawaii");
      propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
      return propertiesSchemaT;
    }
    public static Map<String, java.lang.Object> propertiesSchemaA() {
      Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "ChickenSoup");
      propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<WeaviateObject> objectA, Result<Boolean> deleteObjT, Result<List<WeaviateObject>> objTlist,
      Result<Boolean> deleteObjA, Result<List<WeaviateObject>> objAlist) {
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
  }

  public static class testDataUpdate {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Random");
      propertiesSchemaT.put("description", "Missing description");
      return propertiesSchemaT;
    }
    public static Map<String, java.lang.Object> propertiesSchemaA() {
      Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "water");
      propertiesSchemaA.put("description", "missing description");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<WeaviateObject> objectA, Result<Boolean> updateObjectT,
      Result<Boolean> updateObjectA, Result<List<WeaviateObject>> updatedObjsT, Result<List<WeaviateObject>> updatedObjsA) {
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
  }

  public static class testDataMerge {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Hawaii");
      propertiesSchemaT.put("description", "Missing description");
      return propertiesSchemaT;
    }
    public static Map<String, java.lang.Object> propertiesSchemaA() {
      Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "ChickenSoup");
      propertiesSchemaA.put("description", "missing description");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<WeaviateObject> objectA, Result<Boolean> mergeObjectT, Result<Boolean> mergeObjectA,
      Result<List<WeaviateObject>> mergedObjsT, Result<List<WeaviateObject>> mergeddObjsA) {
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
  }

  public static class testDataValidate {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Hawaii");
      propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
      return propertiesSchemaT;
    }
    public static Map<String, java.lang.Object> propertiesSchemaA() {
      Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "ChickenSoup");
      propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<Boolean> validateObjT, Result<Boolean> validateObjA, Result<Boolean> validateObjT1, Result<Boolean> validateObjA1) {
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
  }

  public static class testDataGetWithAdditionalError {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static Map<String, Object> propertiesSchemaT() {
      Map<String, Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Hawaii");
      propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
      return propertiesSchemaT;
    }
    public static Map<String, Object> propertiesSchemaA() {
      Map<String, Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "ChickenSoup");
      propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<WeaviateObject> objectA, Result<List<WeaviateObject>> objsAdditionalT) {
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

  public static class testDataCreateWithArrayType {
    public static WeaviateClass clazz = WeaviateClass.builder()
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
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("stringArray", new String[]{"a", "b"});
      propertiesSchemaT.put("textArray", new String[]{"c", "d"});
      propertiesSchemaT.put("intArray", new Integer[]{1, 2});
      propertiesSchemaT.put("numberArray", new Float[]{3.3f, 4.4f});
      propertiesSchemaT.put("booleanArray", new Boolean[]{true, false});
      return propertiesSchemaT;
    }
    public static void assertResults(Result<Boolean> createStatus, Result<Schema> schemaAfterCreate, Result<WeaviateObject> objectT,
      Result<List<WeaviateObject>> objectsT, Result<Boolean> deleteStatus, Result<Schema> schemaAfterDelete) {
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
  }

  public static class testDataGetWithVector {
    public static WeaviateClass clazz = WeaviateClass.builder()
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
    public static String objTID = "addfd256-8574-442b-9293-9205193737ee";
    public static Map<String, Object> propertiesSchemaT() {
      Map<String, Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("foo", "bar");
      return propertiesSchemaT;
    }
    public static Float[] vectorObjT = new Float[]{-0.26736435f, -0.112380296f, 0.29648793f, 0.39212644f, 0.0033650293f, -0.07112332f, 0.07513781f, 0.22459874f};
    public static void assertResults(Result<Boolean> createStatus, Result<Schema> schemaAfterCreate, Result<WeaviateObject> objectT, Result<List<WeaviateObject>> objT,
      Result<Boolean> deleteStatus, Result<Schema> schemaAfterDelete) {
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
  }

  public static class testObjectCheck {
    public static String objTID = "abefd256-8574-442b-9293-9205193737ee";
    public static String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    public static String nonExistentObjectID = "11111111-1111-1111-aaaa-aaaaaaaaaaaa";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "Hawaii");
      propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
      return propertiesSchemaT;
    }
    public static Map<String, java.lang.Object> propertiesSchemaA() {
      Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
      propertiesSchemaA.put("name", "ChickenSoup");
      propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      return propertiesSchemaA;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<WeaviateObject> objectA, Result<Boolean> checkObjT, Result<Boolean> checkObjA,
      Result<List<WeaviateObject>> objA, Result<List<WeaviateObject>> objT, Result<Boolean> checkNonexistentObject, Result<Boolean> deleteStatus,
      Result<Boolean> checkObjTAfterDelete, Result<Boolean> checkObjAAfterDelete) {
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
  }

  public static class testDataCreateWithIDInNotUUIDFormat {
    public static String objID = "TODO_4";
    public static Map<String, java.lang.Object> propertiesSchemaT() {
      Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
      propertiesSchemaT.put("name", "name");
      propertiesSchemaT.put("description", "description");
      return propertiesSchemaT;
    }
    public static void assertResults(Result<WeaviateObject> objectT, Result<List<WeaviateObject>> objectsT, Result<Boolean> deleteStatus, Result<Schema> schemaAfterDelete) {
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
  }

  public static class testDataGetUsingClassParameter {
    public static void assertResults(Result<WeaviateObject> pizzaObj1, Result<WeaviateObject> pizzaObj2, Result<WeaviateObject> soupObj1, Result<WeaviateObject> soupObj2,
      Result<List<WeaviateObject>> objects, Result<List<WeaviateObject>> pizzaObjects, Result<List<WeaviateObject>> soupObjects) {
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
  }

  private static void assertCreated(Result<WeaviateObject> obj) {
    assertNotNull(obj);
    assertNotNull(obj.getResult());
    assertNotNull(obj.getResult().getId());
  }

  private static void checkArrays(Object property, int size, Object... contains) {
    assertNotNull(property);
    assertEquals(ArrayList.class, property.getClass());
    List l = (List) property;
    assertEquals(size, l.size());
    for (Object c : contains) {
      assertTrue(l.contains(c));
    }
  }
}
