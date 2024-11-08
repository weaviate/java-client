package io.weaviate.integration.client.async.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.Tokenization;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.tests.schema.SchemaTestSuite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientSchemaTest {
  private WeaviateClient syncClient;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    syncClient = new WeaviateClient(config);
  }

  @After
  public void after() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      client.schema().allDeleter().run(new FutureCallback<Result<Boolean>>() {
        @Override
        public void completed(Result<Boolean> deleted) {
          assertThat(deleted.hasErrors()).isFalse();
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {

        }
      }).get();
    }
  }

  @Test
  public void testSchemaCreateBandClass() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass clazz = SchemaTestSuite.testSchemaCreateBandClass.clazz;
      // when
      Future<Result<Boolean>> createStatusFuture = client.schema().classCreator().withClass(clazz).run();
      Result<Boolean> createStatus = createStatusFuture.get();
      Future<Result<Schema>> schemaFuture = client.schema().getter().run();
      Result<Schema> schema = schemaFuture.get();

      // then
      SchemaTestSuite.testSchemaCreateBandClass.assertResults(createStatus, schema);
    }
  }

  @Test
  public void testSchemaCreateRunClass() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass clazz = SchemaTestSuite.testSchemaCreateRunClass.clazz;
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();
      // then
      SchemaTestSuite.testSchemaCreateRunClass
        .assertResults(createStatus, schemaAfterCreate, deleteStatus, schemaAfterDelete);
    }
  }

  @Test
  public void testSchemaDeleteClasses() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = syncClient.async()) {
      // given
      WeaviateClass pizza = SchemaTestSuite.testSchemaDeleteClasses.pizza;
      WeaviateClass chickenSoup = SchemaTestSuite.testSchemaDeleteClasses.chickenSoup;
      // when
      asyncClient.schema().classCreator().withClass(pizza).run(new FutureCallback<Result<Boolean>>() {
        @Override
        public void completed(Result<Boolean> pizzaCreateStatus) {
          assertNotNull(pizzaCreateStatus);
          assertTrue(pizzaCreateStatus.getResult());
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {
        }
      }).get();
      asyncClient.schema().classCreator().withClass(chickenSoup).run(new FutureCallback<Result<Boolean>>() {
        @Override
        public void completed(Result<Boolean> chickenSoupCreateStatus) {
          assertNotNull(chickenSoupCreateStatus);
          assertTrue(chickenSoupCreateStatus.getResult());
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {

        }
      }).get();
      asyncClient.schema().getter().run(new FutureCallback<Result<Schema>>() {
        @Override
        public void completed(Result<Schema> schemaAfterCreate) {
          assertNotNull(schemaAfterCreate);
          assertNotNull(schemaAfterCreate.getResult());
          assertNotNull(schemaAfterCreate.getResult().getClasses());
          assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
          assertEquals(1, schemaAfterCreate.getResult().getClasses().stream().filter(o -> o.getClassName().equals(pizza.getClassName())).count());
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {

        }
      }).get();

      asyncClient.schema().allDeleter().run(new FutureCallback<Result<Boolean>>() {
        @Override
        public void completed(Result<Boolean> result) {
          assertNotNull(result);
          assertTrue(result.getResult());
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {

        }
      }).get();


      asyncClient.schema().getter().run(new FutureCallback<Result<Schema>>() {
        @Override
        public void completed(Result<Schema> schemaResult) {
          assertNotNull(schemaResult);
          assertNotNull(schemaResult.getResult());
          assertThat(schemaResult.getResult().getClasses()).isNullOrEmpty();
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {

        }
      }).get();
    }
  }

  @Test
  public void testSchemaDeleteAllSchema() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass pizza = SchemaTestSuite.testSchemaDeleteAllSchema.pizza;
      WeaviateClass chickenSoup = SchemaTestSuite.testSchemaDeleteAllSchema.chickenSoup;
      // when
      Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run().get();
      Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<Boolean> deleteAllStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();
      // then
      SchemaTestSuite.testSchemaDeleteAllSchema.assertResults(pizzaCreateStatus, chickenSoupCreateStatus,
        schemaAfterCreate, deleteAllStatus, schemaAfterDelete);
    }
  }

  @Test
  public void testSchemaCreateClassesAddProperties() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass pizza = SchemaTestSuite.testSchemaCreateClassesAddProperties.pizza;
      WeaviateClass chickenSoup = SchemaTestSuite.testSchemaCreateClassesAddProperties.chickenSoup;
      Property newProperty = SchemaTestSuite.testSchemaCreateClassesAddProperties.newProperty;
      // when
      Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run().get();
      Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run().get();
      Result<Boolean> pizzaPropertyCreateStatus = client.schema().propertyCreator()
        .withProperty(newProperty).withClassName(pizza.getClassName()).run().get();
      Result<Boolean> chickenSoupPropertyCreateStatus = client.schema().propertyCreator()
        .withProperty(newProperty).withClassName(chickenSoup.getClassName()).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<Boolean> deleteAllStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();

      // then
      SchemaTestSuite.testSchemaCreateClassesAddProperties.assertResults(pizzaCreateStatus, chickenSoupCreateStatus, pizzaPropertyCreateStatus,
        chickenSoupPropertyCreateStatus, schemaAfterCreate, deleteAllStatus, schemaAfterDelete);
    }
  }

  @Test
  public void testSchemaCreateClassExplicitVectorizerWithProperties() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      Map<String, Object> text2vecContextionary = new HashMap<>();
      text2vecContextionary.put("vectorizeClassName", false);
      Map<String, Object> moduleConfig = new HashMap<>();
      moduleConfig.put("text2vec-contextionary", text2vecContextionary);

      WeaviateClass clazz = WeaviateClass.builder()
        .className("Article")
        .description("A written text, for example a news article or blog post")
        .vectorIndexType("hnsw")
        .vectorizer("text2vec-contextionary")
        .moduleConfig(moduleConfig)
        .properties(new ArrayList() {{
          add(Property.builder()
            .dataType(new ArrayList() {{
              add(DataType.TEXT);
            }})
            .description("Title of the article")
            .name("title")
            .tokenization(Tokenization.FIELD)
            .build());
          add(Property.builder()
            .dataType(new ArrayList() {{
              add(DataType.TEXT);
            }})
            .description("The content of the article")
            .name("content")
            .tokenization(Tokenization.WORD)
            .build());
        }})
        .build();
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();

      // then
      assertResultTrue(createStatus);
      assertClassesSize(1, schemaAfterCreate);

      WeaviateClass resultArticleClass = schemaAfterCreate.getResult().getClasses().get(0);
      assertClassEquals(clazz.getClassName(), clazz.getDescription(), resultArticleClass);

      assertThat(resultArticleClass.getModuleConfig())
        .asInstanceOf(MAP)
        .containsOnlyKeys("text2vec-contextionary")
        .extracting(m -> m.get("text2vec-contextionary"))
        .asInstanceOf(MAP)
        .containsOnlyKeys("vectorizeClassName")
        .extracting(m -> m.get("vectorizeClassName"))
        .isEqualTo(false);

      assertPropertiesSize(2, resultArticleClass);
      assertPropertyEquals("title", "field", resultArticleClass.getProperties().get(0));
      assertPropertyEquals("content", "word", resultArticleClass.getProperties().get(1));

      assertResultTrue(deleteStatus);
      assertClassesSize(0, schemaAfterDelete);
    }
  }

  @Test
  public void testSchemaCreateClassExplicitVectorizerWithArrayProperties() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
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
            .tokenization(Tokenization.FIELD)
            .build());
          add(Property.builder()
            .dataType(new ArrayList() {{
              add(DataType.TEXT_ARRAY);
            }})
            .name("textArray")
            .tokenization(Tokenization.WORD)
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
          add(Property.builder()
            .dataType(new ArrayList() {{
              add(DataType.DATE_ARRAY);
            }})
            .name("dateArray")
            .build());
        }})
        .build();
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();

      // then
      assertResultTrue(createStatus);
      assertClassesSize(1, schemaAfterCreate);

      WeaviateClass resultArraysClass = schemaAfterCreate.getResult().getClasses().get(0);
      assertClassEquals(clazz.getClassName(), clazz.getDescription(), resultArraysClass);
      assertPropertiesSize(6, resultArraysClass);
      assertPropertyEquals("stringArray", DataType.TEXT_ARRAY, "field", resultArraysClass.getProperties().get(0));
      assertPropertyEquals("textArray", DataType.TEXT_ARRAY, "word", resultArraysClass.getProperties().get(1));
      assertPropertyEquals("intArray", DataType.INT_ARRAY, null, resultArraysClass.getProperties().get(2));
      assertPropertyEquals("numberArray", DataType.NUMBER_ARRAY, null, resultArraysClass.getProperties().get(3));
      assertPropertyEquals("booleanArray", DataType.BOOLEAN_ARRAY, null, resultArraysClass.getProperties().get(4));
      assertPropertyEquals("dateArray", DataType.DATE_ARRAY, null, resultArraysClass.getProperties().get(5));

      assertResultTrue(deleteStatus);
      assertClassesSize(0, schemaAfterDelete);
    }
  }

  @Test
  public void testSchemaCreateClassWithProperties() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass clazz = WeaviateClass.builder()
        .className("Article")
        .description("A written text, for example a news article or blog post")
        .properties(new ArrayList() {{
          add(Property.builder()
            .dataType(new ArrayList() {{
              add(DataType.TEXT);
            }})
            .description("Title of the article")
            .name("title")
            .build());
          add(Property.builder()
            .dataType(new ArrayList() {{
              add(DataType.TEXT);
            }})
            .description("The content of the article")
            .name("content")
            .build());
        }})
        .build();
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<Schema> schemaAfterCreate = client.schema().getter().run().get();
      Result<Boolean> deleteStatus = client.schema().allDeleter().run().get();
      Result<Schema> schemaAfterDelete = client.schema().getter().run().get();

      // then
      assertResultTrue(createStatus);
      assertClassesSize(1, schemaAfterCreate);

      WeaviateClass resultArticleClass = schemaAfterCreate.getResult().getClasses().get(0);
      assertClassEquals(clazz.getClassName(), clazz.getDescription(), resultArticleClass);
      assertPropertiesSize(2, resultArticleClass);
      assertPropertyEquals("title", "word", resultArticleClass.getProperties().get(0));
      assertPropertyEquals("content", "word", resultArticleClass.getProperties().get(1));

      assertResultTrue(deleteStatus);
      assertClassesSize(0, schemaAfterDelete);
    }
  }

  @Test
  public void testSchemaCreateClassWithInvalidTokenizationProperty() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass pizza = WeaviateClass.builder()
        .className("Pizza")
        .description("A delicious religion like food and arguably the best export of Italy.")
        .build();

      Property notExistingTokenization = Property.builder()
        .dataType(Collections.singletonList(DataType.TEXT))
        .description("someString")
        .name("someString")
        .tokenization("not-existing")
        .build();
      Property notSupportedTokenizationForInt = Property.builder()
        .dataType(Collections.singletonList(DataType.INT))
        .description("someInt")
        .name("someInt")
        .tokenization(Tokenization.WORD)
        .build();
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(pizza).run().get();
      Result<Boolean> notExistingTokenizationCreateStatus = client.schema().propertyCreator()
        .withProperty(notExistingTokenization).withClassName(pizza.getClassName()).run().get();
      Result<Boolean> notSupportedTokenizationForIntCreateStatus = client.schema().propertyCreator()
        .withProperty(notSupportedTokenizationForInt).withClassName(pizza.getClassName()).run().get();

      //then
      assertResultTrue(createStatus);

      assertResultError("tokenization in body should be one of [word lowercase whitespace field trigram gse kagome_kr]", notExistingTokenizationCreateStatus);
      assertResultError("Tokenization is not allowed for data type 'int'", notSupportedTokenizationForIntCreateStatus);
    }
  }

  private void assertResultTrue(Result<Boolean> result) {
    assertNotNull(result);
    assertTrue(result.getResult());
  }

  private void assertClassesSize(int expectedSize, Result<Schema> schemaAfterCreate) {
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(expectedSize, schemaAfterCreate.getResult().getClasses().size());
  }

  private void assertClassEquals(String expectedName, String expectedDescription, WeaviateClass schemaClass) {
    assertEquals(expectedName, schemaClass.getClassName());
    assertEquals(expectedDescription, schemaClass.getDescription());
  }

  private void assertPropertiesSize(int expectedSize, WeaviateClass schemaClass) {
    assertNotNull(schemaClass.getProperties());
    assertEquals(expectedSize, schemaClass.getProperties().size());
  }

  private void assertPropertyEquals(String expectedName, String expectedTokenization, Property property) {
    assertEquals(expectedName, property.getName());
    assertEquals(expectedTokenization, property.getTokenization());
  }

  private void assertPropertyEquals(String expectedName, String expectedDataType, String expectedTokenization, Property property) {
    assertPropertyEquals(expectedName, expectedTokenization, property);
    assertTrue(property.getDataType().size() > 0);
    assertEquals(expectedDataType, property.getDataType().get(0));
  }

  private void assertResultError(String msg, Result<?> result) {
    assertNotNull(result);
    assertTrue(result.hasErrors());
    List<WeaviateErrorMessage> messages = result.getError().getMessages();
    assertEquals(1, messages.size());
    assertEquals(msg, messages.get(0).getMessage());
  }
}
