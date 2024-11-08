package io.weaviate.integration.tests.schema;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.Tokenization;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SchemaTestSuite {
  public static class testSchemaCreateBandClass {
    public static WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .build();

    public static void assertResults(Result<Boolean> createStatus, Result<Schema> schema) {
      assertNotNull(createStatus);
      assertTrue(createStatus.getResult());
      assertNotNull(schema);
      assertNotNull(schema.getResult());
      assertEquals(1, schema.getResult().getClasses().size());

      WeaviateClass resultClass = schema.getResult().getClasses().get(0);
      assertEquals(clazz.getClassName(), resultClass.getClassName());
      assertEquals(clazz.getDescription(), resultClass.getDescription());
    }
  }

  public static class testSchemaCreateRunClass {
    public static WeaviateClass clazz = WeaviateClass.builder()
      .className("Run")
      .description("Running from the fuzz")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .build();

    public static void assertResults(Result<Boolean> createStatus, Result<Schema> schemaAfterCreate, Result<Boolean> deleteStatus,
      Result<Schema> schemaAfterDelete) {
      assertNotNull(createStatus);
      assertTrue(createStatus.getResult());
      assertNotNull(schemaAfterCreate);
      assertNotNull(schemaAfterCreate.getResult());
      assertEquals(1, schemaAfterCreate.getResult().getClasses().size());
      assertEquals(clazz.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
      assertEquals(clazz.getDescription(), schemaAfterCreate.getResult().getClasses().get(0).getDescription());
      assertNotNull(deleteStatus);
      assertTrue(deleteStatus.getResult());
      assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
    }
  }

  public static class testSchemaDeleteClasses {
    public static WeaviateClass pizza = WeaviateClass.builder()
      .className("Pizza")
      .description("A delicious religion like food and arguably the best export of Italy.")
      .build();
    public static WeaviateClass chickenSoup = WeaviateClass.builder()
      .className("ChickenSoup")
      .description("A soup made in part out of chicken, not for chicken.")
      .build();

    public static void assertResults(Result<Boolean> pizzaCreateStatus,
      Result<Boolean> chickenSoupCreateStatus,
      Result<Schema> schemaAfterCreate,
      Result<Boolean> deletePizzaStatus,
      Result<Boolean> deleteChickenSoupStatus,
      Result<Schema> schemaAfterDelete
    ) {
      assertNotNull(pizzaCreateStatus);
      assertTrue(pizzaCreateStatus.getResult());
      assertNotNull(chickenSoupCreateStatus);
      assertTrue(chickenSoupCreateStatus.getResult());
      assertNotNull(schemaAfterCreate);
      assertNotNull(schemaAfterCreate.getResult());
      assertNotNull(schemaAfterCreate.getResult().getClasses());
      assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
      assertEquals(1, schemaAfterCreate.getResult().getClasses().stream().filter(o -> o.getClassName().equals(pizza.getClassName())).count());
      assertNotNull(deletePizzaStatus);
      assertTrue(deletePizzaStatus.getResult());
      assertNotNull(deleteChickenSoupStatus);
      assertTrue(deleteChickenSoupStatus.getResult());
      assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
    }
  }

  public static class testSchemaDeleteAllSchema {
    public static WeaviateClass pizza = WeaviateClass.builder()
      .className("Pizza")
      .description("A delicious religion like food and arguably the best export of Italy.")
      .build();
    public static WeaviateClass chickenSoup = WeaviateClass.builder()
      .className("ChickenSoup")
      .description("A soup made in part out of chicken, not for chicken.")
      .build();
    public static void assertResults(Result<Boolean> pizzaCreateStatus, Result<Boolean> chickenSoupCreateStatus,
      Result<Schema> schemaAfterCreate, Result<Boolean> deleteAllStatus, Result<Schema> schemaAfterDelete) {
      assertNotNull(pizzaCreateStatus);
      assertTrue(pizzaCreateStatus.getResult());
      assertNotNull(chickenSoupCreateStatus);
      assertTrue(chickenSoupCreateStatus.getResult());
      assertNotNull(schemaAfterCreate);
      assertNotNull(schemaAfterCreate.getResult());
      assertNotNull(schemaAfterCreate.getResult().getClasses());
      assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
      assertEquals(1, schemaAfterCreate.getResult().getClasses().stream().filter(o -> o.getClassName().equals(pizza.getClassName())).count());
      assertEquals(1, schemaAfterCreate.getResult().getClasses().stream().filter(o -> o.getDescription().equals(chickenSoup.getDescription())).count());
      assertNotNull(deleteAllStatus);
      assertTrue(deleteAllStatus.getResult());
      assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
    }
  }

  public static class testSchemaCreateClassesAddProperties {
    public static WeaviateClass pizza = WeaviateClass.builder()
      .className("Pizza")
      .description("A delicious religion like food and arguably the best export of Italy.")
      .build();
    public static WeaviateClass chickenSoup = WeaviateClass.builder()
      .className("ChickenSoup")
      .description("A soup made in part out of chicken, not for chicken.")
      .build();
    public static Property newProperty = Property.builder()
      .dataType(Arrays.asList(DataType.TEXT))
      .description("name")
      .name("name")
      .build();
    public static void assertResults(Result<Boolean> pizzaCreateStatus, Result<Boolean> chickenSoupCreateStatus, Result<Boolean> pizzaPropertyCreateStatus,
      Result<Boolean> chickenSoupPropertyCreateStatus, Result<Schema> schemaAfterCreate, Result<Boolean> deleteAllStatus, Result<Schema> schemaAfterDelete) {
      assertResultTrue(pizzaCreateStatus);
      assertResultTrue(chickenSoupCreateStatus);
      assertResultTrue(pizzaPropertyCreateStatus);
      assertResultTrue(chickenSoupPropertyCreateStatus);
      assertClassesSize(2, schemaAfterCreate);

      WeaviateClass resultPizzaClass = schemaAfterCreate.getResult().getClasses()
        .stream().filter(o -> o.getClassName().equals(pizza.getClassName())).findFirst().get();
      assertClassEquals(pizza.getClassName(), pizza.getDescription(), resultPizzaClass);
      assertPropertiesSize(1, resultPizzaClass);
      assertPropertyEquals(newProperty.getName(), "word", resultPizzaClass.getProperties().get(0));
      WeaviateClass resultChickenSoupClass = schemaAfterCreate.getResult().getClasses()
        .stream().filter(o -> o.getClassName().equals(chickenSoup.getClassName())).findFirst().get();
      assertClassEquals(chickenSoup.getClassName(), chickenSoup.getDescription(), resultChickenSoupClass);
      assertPropertiesSize(1, resultChickenSoupClass);
      assertPropertyEquals(newProperty.getName(), "word", resultChickenSoupClass.getProperties().get(0));

      assertResultTrue(deleteAllStatus);
      assertClassesSize(0, schemaAfterDelete);
    }
  }

  public static void assertResultTrue(Result<Boolean> result) {
    assertNotNull(result);
    assertTrue(result.getResult());
  }

  public static void assertClassesSize(int expectedSize, Result<Schema> schemaAfterCreate) {
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(expectedSize, schemaAfterCreate.getResult().getClasses().size());
  }

  public static void assertClassEquals(String expectedName, String expectedDescription, WeaviateClass schemaClass) {
    assertEquals(expectedName, schemaClass.getClassName());
    assertEquals(expectedDescription, schemaClass.getDescription());
  }

  public static void assertPropertiesSize(int expectedSize, WeaviateClass schemaClass) {
    assertNotNull(schemaClass.getProperties());
    assertEquals(expectedSize, schemaClass.getProperties().size());
  }

  public static void assertPropertyEquals(String expectedName, String expectedTokenization, Property property) {
    assertEquals(expectedName, property.getName());
    assertEquals(expectedTokenization, property.getTokenization());
  }

  public static void assertPropertyEquals(String expectedName, String expectedDataType, String expectedTokenization, Property property) {
    assertPropertyEquals(expectedName, expectedTokenization, property);
    assertTrue(property.getDataType().size() > 0);
    assertEquals(expectedDataType, property.getDataType().get(0));
  }

  public static void assertResultError(String msg, Result<?> result) {
    assertNotNull(result);
    assertTrue(result.hasErrors());
    List<WeaviateErrorMessage> messages = result.getError().getMessages();
    assertEquals(1, messages.size());
    assertEquals(msg, messages.get(0).getMessage());
  }
}
