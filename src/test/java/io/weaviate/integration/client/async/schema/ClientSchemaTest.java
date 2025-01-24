package io.weaviate.integration.client.async.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.misc.model.PQConfig;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.misc.model.ShardingConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.Shard;
import io.weaviate.client.v1.schema.model.ShardStatus;
import io.weaviate.client.v1.schema.model.ShardStatuses;
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
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass pizza = SchemaTestSuite.testSchemaDeleteClasses.pizza;
      WeaviateClass chickenSoup = SchemaTestSuite.testSchemaDeleteClasses.chickenSoup;
      // when
      client.schema().classCreator().withClass(pizza).run(new FutureCallback<Result<Boolean>>() {
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
      client.schema().classCreator().withClass(chickenSoup).run(new FutureCallback<Result<Boolean>>() {
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
      client.schema().getter().run(new FutureCallback<Result<Schema>>() {
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

      client.schema().allDeleter().run(new FutureCallback<Result<Boolean>>() {
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


      client.schema().getter().run(new FutureCallback<Result<Schema>>() {
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
        .properties(new ArrayList<Property>() {{
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
              add(DataType.TEXT);
            }})
            .description("Title of the article")
            .name("title")
            .tokenization(Tokenization.FIELD)
            .build());
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
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
        .properties(new ArrayList<Property>() {{
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
              add(DataType.TEXT_ARRAY);
            }})
            .name("stringArray")
            .tokenization(Tokenization.FIELD)
            .build());
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
              add(DataType.TEXT_ARRAY);
            }})
            .name("textArray")
            .tokenization(Tokenization.WORD)
            .build());
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
              add(DataType.INT_ARRAY);
            }})
            .name("intArray")
            .build());
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
              add(DataType.NUMBER_ARRAY);
            }})
            .name("numberArray")
            .build());
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
              add(DataType.BOOLEAN_ARRAY);
            }})
            .name("booleanArray")
            .build());
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
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
        .properties(new ArrayList<Property>() {{
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
              add(DataType.TEXT);
            }})
            .description("Title of the article")
            .name("title")
            .build());
          add(Property.builder()
            .dataType(new ArrayList<String>() {{
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

      assertResultError("tokenization in body should be one of [word lowercase whitespace field trigram gse kagome_kr kagome_ja]", notExistingTokenizationCreateStatus);
      assertResultError("Tokenization is not allowed for data type 'int'", notSupportedTokenizationForIntCreateStatus);
    }
  }

  @Test
  public void testSchemaGetBandClass() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass clazz = WeaviateClass.builder()
        .className("Band")
        .description("Band that plays and produces music")
        .vectorIndexType("hnsw")
        .vectorizer("text2vec-contextionary")
        .build();
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run().get();
      Result<WeaviateClass> nonExistentClass = client.schema().classGetter().withClassName("nonExistentClass").run().get();
      // then
      assertNotNull(createStatus);
      assertTrue(createStatus.getResult());
      assertNotNull(bandClass);
      assertNotNull(bandClass.getResult());
      assertNull(bandClass.getError());
      assertEquals(clazz.getClassName(), bandClass.getResult().getClassName());
      assertEquals(clazz.getDescription(), bandClass.getResult().getDescription());
      assertEquals(clazz.getVectorIndexType(), bandClass.getResult().getVectorIndexType());
      assertEquals(clazz.getVectorizer(), bandClass.getResult().getVectorizer());
      assertNotNull(nonExistentClass);
      assertNull(nonExistentClass.getError());
      assertNull(nonExistentClass.getResult());
    }
  }

  @Test
  public void testSchemaGetShards() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      WeaviateClass clazz = WeaviateClass.builder()
        .className("Band")
        .description("Band that plays and produces music")
        .vectorIndexType("hnsw")
        .vectorizer("text2vec-contextionary")
        .build();
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      Result<Boolean> bandClassExists = client.schema().exists().withClassName(clazz.getClassName()).run().get();
      Result<Boolean> nonExistentClassExists = client.schema().exists().withClassName("nonExistentClass").run().get();
      // then
      assertResultTrue(createStatus);
      assertResultTrue(bandClassExists);
      assertNotNull(nonExistentClassExists);
      assertFalse(nonExistentClassExists.getResult());
      assertNull(nonExistentClassExists.getError());
      Result<Shard[]> shards = client.schema().shardsGetter()
        .withClassName(clazz.getClassName()).run().get();
      assertNotNull(shards);
      assertNotNull(shards.getResult());
      assertEquals(1, shards.getResult().length);
      Shard shard = shards.getResult()[0];
      assertNotNull(shard.getName());
      assertNotNull(shard.getStatus());
    }
  }

  @Test
  public void shouldUpdateClass() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      String className = "Question";
      List<Property> properties = Arrays.asList(
        Property.builder()
          .name("question")
          .dataType(Arrays.asList(DataType.TEXT))
          .build(),
        Property.builder()
          .name("answer")
          .dataType(Arrays.asList(DataType.TEXT))
          .build()
      );

      WeaviateClass jeopardyClass = WeaviateClass.builder()
        .className(className)
        .description("A Jeopardy! question")
        .vectorizer("text2vec-contextionary")
        .properties(properties)
        .build();

      Result<Boolean> createResult = client.schema().classCreator()
        .withClass(jeopardyClass)
        .run().get();

      assertThat(createResult).isNotNull()
        .withFailMessage(() -> createResult.getError().toString())
        .returns(false, Result::hasErrors)
        .withFailMessage(null)
        .returns(true, Result::getResult);

      Result<WeaviateClass> createdClassResult = client.schema().classGetter()
        .withClassName(className)
        .run().get();

      assertThat(createdClassResult).isNotNull()
        .withFailMessage(() -> createdClassResult.getError().toString())
        .returns(false, Result::hasErrors)
        .withFailMessage(null)
        .extracting(Result::getResult).isNotNull()
        .extracting(WeaviateClass::getVectorIndexConfig).isNotNull()
        .extracting(VectorIndexConfig::getPq).isNotNull()
        .returns(false, PQConfig::getEnabled);

      WeaviateClass newJeopardyClass = WeaviateClass.builder()
        .className(className)
        .vectorizer("text2vec-contextionary")
        .properties(properties)
        .vectorIndexConfig(VectorIndexConfig.builder()
          .filterStrategy(VectorIndexConfig.FilterStrategy.ACORN)
          .pq(PQConfig.builder()
            .enabled(true)
            .trainingLimit(99_999)
            .segments(96)
            .build())
          .build())
        .replicationConfig(ReplicationConfig.builder()
          .deletionStrategy(ReplicationConfig.DeletionStrategy.DELETE_ON_CONFLICT)
          .build())
        .build();

      Result<Boolean> updateResult = client.schema().classUpdater()
        .withClass(newJeopardyClass)
        .run().get();

      assertThat(updateResult).isNotNull()
        .withFailMessage(() -> updateResult.getError().toString())
        .returns(false, Result::hasErrors)
        .withFailMessage(null)
        .returns(true, Result::getResult);

      Result<WeaviateClass> updatedClassResult = client.schema().classGetter()
        .withClassName(className)
        .run().get();

      assertThat(updatedClassResult).isNotNull()
        .withFailMessage(() -> updatedClassResult.getError().toString())
        .returns(false, Result::hasErrors)
        .withFailMessage(null)
        .extracting(Result::getResult).isNotNull()
        .extracting(WeaviateClass::getVectorIndexConfig).isNotNull()
        .returns(VectorIndexConfig.FilterStrategy.ACORN, VectorIndexConfig::getFilterStrategy)
        .extracting(VectorIndexConfig::getPq).isNotNull()
        .returns(true, PQConfig::getEnabled)
        .returns(96, PQConfig::getSegments)
        .returns(99_999, PQConfig::getTrainingLimit);

      assertThat(updatedClassResult.getResult())
        .extracting(WeaviateClass::getReplicationConfig).isNotNull()
        .returns(ReplicationConfig.DeletionStrategy.DELETE_ON_CONFLICT, ReplicationConfig::getDeletionStrategy);
    }
  }

  @Test
  public void testSchemaUpdateShards()  throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient client = syncClient.async()) {
      // given
      String className = "Band";
      int shardCount = 3;
      ShardingConfig shardingConfig = ShardingConfig.builder()
        .actualCount(shardCount)
        .actualVirtualCount(128)
        .desiredCount(shardCount)
        .desiredVirtualCount(128)
        .function("murmur3")
        .key("_id")
        .strategy("hash")
        .virtualPerPhysical(128)
        .build();
      WeaviateClass clazz = WeaviateClass.builder()
        .className(className)
        .description("Band that plays and produces music")
        .vectorIndexType("hnsw")
        .vectorizer("text2vec-contextionary")
        .shardingConfig(shardingConfig)
        .build();
      // when
      Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run().get();
      assertResultTrue(createStatus);
      // then
      Result<Shard[]> shards = client.schema().shardsGetter().withClassName(className).run().get();
      assertNotNull(shards);
      assertNull(shards.getError());
      assertNotNull(shards.getResult());
      assertEquals(3, shards.getResult().length);
      // update shard status to READONLY
      Result<ShardStatus[]> updateToREADONLY = client.schema().shardsUpdater()
        .withClassName(className)
        .withStatus(ShardStatuses.READONLY)
        .run().get();
      assertNotNull(updateToREADONLY.getResult());
      assertEquals(3, updateToREADONLY.getResult().length);
      for (ShardStatus s : updateToREADONLY.getResult()) {
        assertEquals(ShardStatuses.READONLY, s.getStatus());
      }
      // update shard status to READY
      Result<ShardStatus[]> updateToREADY = client.schema().shardsUpdater()
        .withClassName(className)
        .withStatus(ShardStatuses.READY)
        .run().get();
      assertNotNull(updateToREADY.getResult());
      assertEquals(3, updateToREADY.getResult().length);
      for (ShardStatus s : updateToREADY.getResult()) {
        assertEquals(ShardStatuses.READY, s.getStatus());
      }
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
