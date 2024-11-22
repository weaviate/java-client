package io.weaviate.integration.client.schema;

import com.google.gson.Gson;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.BM25Config;
import io.weaviate.client.v1.misc.model.DistanceType;
import io.weaviate.client.v1.misc.model.InvertedIndexConfig;
import io.weaviate.client.v1.misc.model.PQConfig;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.misc.model.ShardingConfig;
import io.weaviate.client.v1.misc.model.StopwordConfig;
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
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import org.junit.After;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;


public class ClientSchemaTest {
  private WeaviateClient client;
  private final NestedObjectsUtils utils = new NestedObjectsUtils();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
  }

  @After
  public void after() {
    Result<Boolean> deleted = client.schema().allDeleter().run();
    assertThat(deleted.hasErrors()).isFalse();
  }

  @Test
  public void testSchemaCreateBandClass() {
    // given
    WeaviateClass clazz = SchemaTestSuite.testSchemaCreateBandClass.clazz;
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schema = client.schema().getter().run();
    // then
    SchemaTestSuite.testSchemaCreateBandClass.assertResults(createStatus, schema);
  }

  @Test
  public void testSchemaCreateRunClass() {
    // given
    WeaviateClass clazz = SchemaTestSuite.testSchemaCreateRunClass.clazz;
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    SchemaTestSuite.testSchemaCreateRunClass
      .assertResults(createStatus, schemaAfterCreate, deleteStatus, schemaAfterDelete);
  }

  @Test
  public void testSchemaDeleteClasses() {
    // given
    WeaviateClass pizza = SchemaTestSuite.testSchemaDeleteClasses.pizza;
    WeaviateClass chickenSoup = SchemaTestSuite.testSchemaDeleteClasses.chickenSoup;
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deletePizzaStatus = client.schema().classDeleter().withClassName(pizza.getClassName()).run();
    Result<Boolean> deleteChickenSoupStatus = client.schema().classDeleter().withClassName(chickenSoup.getClassName()).run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    SchemaTestSuite.testSchemaDeleteClasses.assertResults(pizzaCreateStatus,
      chickenSoupCreateStatus,
      schemaAfterCreate,
      deletePizzaStatus,
      deleteChickenSoupStatus,
      schemaAfterDelete);
  }

  @Test
  public void testSchemaDeleteAllSchema() {
    // given
    WeaviateClass pizza = SchemaTestSuite.testSchemaDeleteAllSchema.pizza;
    WeaviateClass chickenSoup = SchemaTestSuite.testSchemaDeleteAllSchema.chickenSoup;
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    SchemaTestSuite.testSchemaDeleteAllSchema.assertResults(pizzaCreateStatus, chickenSoupCreateStatus,
      schemaAfterCreate, deleteAllStatus, schemaAfterDelete);
  }

  @Test
  public void testSchemaCreateClassesAddProperties() {
    // given
    WeaviateClass pizza = SchemaTestSuite.testSchemaCreateClassesAddProperties.pizza;
    WeaviateClass chickenSoup = SchemaTestSuite.testSchemaCreateClassesAddProperties.chickenSoup;
    Property newProperty = SchemaTestSuite.testSchemaCreateClassesAddProperties.newProperty;
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Boolean> pizzaPropertyCreateStatus = client.schema().propertyCreator()
      .withProperty(newProperty).withClassName(pizza.getClassName()).run();
    Result<Boolean> chickenSoupPropertyCreateStatus = client.schema().propertyCreator()
      .withProperty(newProperty).withClassName(chickenSoup.getClassName()).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    SchemaTestSuite.testSchemaCreateClassesAddProperties.assertResults(pizzaCreateStatus, chickenSoupCreateStatus, pizzaPropertyCreateStatus,
      chickenSoupPropertyCreateStatus, schemaAfterCreate, deleteAllStatus, schemaAfterDelete);
  }

  @Test
  public void testSchemaCreateClassExplicitVectorizerWithProperties() {
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
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();

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

  @Test
  public void testSchemaCreateClassExplicitVectorizerWithArrayProperties() {
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
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();

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

  @Test
  public void testSchemaCreateClassWithProperties() {
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
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();

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

  @Test
  public void testSchemaCreateClassWithInvalidTokenizationProperty() {
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
    Result<Boolean> createStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> notExistingTokenizationCreateStatus = client.schema().propertyCreator()
      .withProperty(notExistingTokenization).withClassName(pizza.getClassName()).run();
    Result<Boolean> notSupportedTokenizationForIntCreateStatus = client.schema().propertyCreator()
      .withProperty(notSupportedTokenizationForInt).withClassName(pizza.getClassName()).run();

    //then
    assertResultTrue(createStatus);

    assertResultError("tokenization in body should be one of [word lowercase whitespace field trigram gse kagome_kr]", notExistingTokenizationCreateStatus);
    assertResultError("Tokenization is not allowed for data type 'int'", notSupportedTokenizationForIntCreateStatus);
  }

  @Test
  public void testCreateClassWithBM25Config() {
    // given
    BM25Config bm25Config = BM25Config.builder()
      .b(0.777f)
      .k1(1.777f)
      .build();

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
      .bm25(bm25Config)
      .build();

    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .invertedIndexConfig(invertedIndexConfig)
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getBm25());
    assertEquals(bm25Config.getB(), bandClass.getResult().getInvertedIndexConfig().getBm25().getB());
    assertEquals(bm25Config.getK1(), bandClass.getResult().getInvertedIndexConfig().getBm25().getK1());
  }

  @Test
  public void testCreateClassWithInvertedIndexContainingIndexNullState() {
    // given
    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
      .indexNullState(true)
      .build();

    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .invertedIndexConfig(invertedIndexConfig)
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertTrue(bandClass.getResult().getInvertedIndexConfig().getIndexNullState());
  }

  @Test
  public void testCreateClassWithInvertedIndexContainingIndexPropertyLength() {
    // given
    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
      .indexPropertyLength(true)
      .build();

    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .invertedIndexConfig(invertedIndexConfig)
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertTrue(bandClass.getResult().getInvertedIndexConfig().getIndexPropertyLength());
  }

  @Test
  public void testCreateClassWithStopwordsConfig() {
    // given
    StopwordConfig stopwordConfig = StopwordConfig.builder()
      .preset("en")
      .additions(new String[]{"star", "nebula"})
      .removals(new String[]{"a", "the"})
      .build();

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
      .stopwords(stopwordConfig)
      .build();

    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .invertedIndexConfig(invertedIndexConfig)
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getStopwords());
    assertEquals(stopwordConfig.getPreset(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getPreset());
    assertArrayEquals(stopwordConfig.getAdditions(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getAdditions());
    assertArrayEquals(stopwordConfig.getRemovals(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getRemovals());
  }

  @Test
  public void testCreateClassWithBM25ConfigAndWithStopwordsConfig() {
    // given
    BM25Config bm25Config = BM25Config.builder()
      .b(0.777f)
      .k1(1.777f)
      .build();

    StopwordConfig stopwordConfig = StopwordConfig.builder()
      .preset("en")
      .additions(new String[]{"star", "nebula"})
      .removals(new String[]{"a", "the"})
      .build();

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
      .bm25(bm25Config)
      .stopwords(stopwordConfig)
      .build();

    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .invertedIndexConfig(invertedIndexConfig)
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getBm25());
    assertEquals(bm25Config.getB(), bandClass.getResult().getInvertedIndexConfig().getBm25().getB());
    assertEquals(bm25Config.getK1(), bandClass.getResult().getInvertedIndexConfig().getBm25().getK1());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getStopwords());
    assertEquals(stopwordConfig.getPreset(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getPreset());
    assertArrayEquals(stopwordConfig.getAdditions(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getAdditions());
    assertArrayEquals(stopwordConfig.getRemovals(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getRemovals());
  }

  @Test
  public void testCreateClassWithInvertedIndexConfigAndVectorIndexConfigAndShardConfig() {
    // given
    BM25Config bm25Config = BM25Config.builder()
      .b(0.777f)
      .k1(1.777f)
      .build();
    StopwordConfig stopwordConfig = StopwordConfig.builder()
      .preset("en")
      .additions(new String[]{"star", "nebula"})
      .removals(new String[]{"a", "the"})
      .build();
    Integer cleanupIntervalSeconds = 300;
    // vector index config
    Integer efConstruction = 128;
    Integer maxConnections = 64;
    Long vectorCacheMaxObjects = 500000L;
    Integer ef = -1;
    Boolean skip = false;
    Integer dynamicEfFactor = 8;
    Integer dynamicEfMax = 500;
    Integer dynamicEfMin = 100;
    Integer flatSearchCutoff = 40000;
    String distance = DistanceType.DOT;
    //pq config
    Boolean enabled = true;
    Boolean bitCompression = true;
    Integer segments = 4;
    Integer centroids = 8;
    String encoderType = "tile";
    String encoderDistribution = "normal";
    // shard config
    Integer actualCount = 1;
    Integer actualVirtualCount = 128;
    Integer desiredCount = 1;
    Integer desiredVirtualCount = 128;
    String function = "murmur3";
    String key = "_id";
    String strategy = "hash";
    Integer virtualPerPhysical = 128;

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
      .bm25(bm25Config)
      .stopwords(stopwordConfig)
      .cleanupIntervalSeconds(cleanupIntervalSeconds)
      .build();

    VectorIndexConfig vectorIndexConfig = VectorIndexConfig.builder()
      .cleanupIntervalSeconds(cleanupIntervalSeconds)
      .efConstruction(efConstruction)
      .maxConnections(maxConnections)
      .vectorCacheMaxObjects(vectorCacheMaxObjects)
      .ef(ef)
      .skip(skip)
      .dynamicEfFactor(dynamicEfFactor)
      .dynamicEfMax(dynamicEfMax)
      .dynamicEfMin(dynamicEfMin)
      .flatSearchCutoff(flatSearchCutoff)
      .distance(distance)
      .pq(PQConfig.builder()
        .enabled(enabled)
        .bitCompression(bitCompression)
        .segments(segments)
        .centroids(centroids)
        .encoder(PQConfig.Encoder.builder()
          .type(encoderType)
          .distribution(encoderDistribution)
          .build())
        .build())
      .build();

    ShardingConfig shardingConfig = ShardingConfig.builder()
      .actualCount(actualCount)
      .actualVirtualCount(actualVirtualCount)
      .desiredCount(desiredCount)
      .desiredVirtualCount(desiredVirtualCount)
      .function(function)
      .key(key)
      .strategy(strategy)
      .virtualPerPhysical(virtualPerPhysical)
      .build();

    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .invertedIndexConfig(invertedIndexConfig)
      .vectorIndexConfig(vectorIndexConfig)
      .shardingConfig(shardingConfig)
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();

    // then
    assertNotNull(createStatus);
    assertNull(createStatus.getError());
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    InvertedIndexConfig classInvertedIndexConfig = bandClass.getResult().getInvertedIndexConfig();
    assertNotNull(classInvertedIndexConfig.getBm25());
    assertEquals(bm25Config.getB(), classInvertedIndexConfig.getBm25().getB());
    assertEquals(bm25Config.getK1(), classInvertedIndexConfig.getBm25().getK1());
    assertNotNull(classInvertedIndexConfig.getStopwords());
    assertEquals(stopwordConfig.getPreset(), classInvertedIndexConfig.getStopwords().getPreset());
    assertArrayEquals(stopwordConfig.getAdditions(), classInvertedIndexConfig.getStopwords().getAdditions());
    assertArrayEquals(stopwordConfig.getRemovals(), classInvertedIndexConfig.getStopwords().getRemovals());
    assertEquals(cleanupIntervalSeconds, classInvertedIndexConfig.getCleanupIntervalSeconds());
    VectorIndexConfig classVectorIndexConfig = bandClass.getResult().getVectorIndexConfig();
    assertEquals(maxConnections, classVectorIndexConfig.getMaxConnections());
    assertEquals(efConstruction, classVectorIndexConfig.getEfConstruction());
    assertEquals(vectorCacheMaxObjects, classVectorIndexConfig.getVectorCacheMaxObjects());
    assertEquals(ef, classVectorIndexConfig.getEf());
    assertEquals(skip, classVectorIndexConfig.getSkip());
    assertEquals(dynamicEfFactor, classVectorIndexConfig.getDynamicEfFactor());
    assertEquals(dynamicEfMax, classVectorIndexConfig.getDynamicEfMax());
    assertEquals(dynamicEfMin, classVectorIndexConfig.getDynamicEfMin());
    assertEquals(flatSearchCutoff, classVectorIndexConfig.getFlatSearchCutoff());
    assertEquals(distance, classVectorIndexConfig.getDistance());

    assertThat(classVectorIndexConfig.getPq())
      .isNotNull()
      .returns(enabled, PQConfig::getEnabled)
      .returns(bitCompression, PQConfig::getBitCompression)
      .returns(segments, PQConfig::getSegments)
      .returns(centroids, PQConfig::getCentroids);
    assertThat(classVectorIndexConfig.getPq().getEncoder())
      .isNotNull()
      .returns(encoderType, PQConfig.Encoder::getType)
      .returns(encoderDistribution, PQConfig.Encoder::getDistribution);

    ShardingConfig classShardingIndexConfig = bandClass.getResult().getShardingConfig();
    assertEquals(actualCount, classShardingIndexConfig.getActualCount());
    assertEquals(actualVirtualCount, classShardingIndexConfig.getActualVirtualCount());
    assertEquals(desiredCount, classShardingIndexConfig.getDesiredCount());
    assertEquals(desiredVirtualCount, classShardingIndexConfig.getDesiredVirtualCount());
    assertEquals(function, classShardingIndexConfig.getFunction());
    assertEquals(key, classShardingIndexConfig.getKey());
    assertEquals(strategy, classShardingIndexConfig.getStrategy());
    assertEquals(virtualPerPhysical, classShardingIndexConfig.getVirtualPerPhysical());
  }

  @Test
  public void testSchemaGetBandClass() {
    // given
    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();
    Result<WeaviateClass> nonExistentClass = client.schema().classGetter().withClassName("nonExistentClass").run();
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

  @Test
  public void testSchemaGetShards() {
    // given
    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Boolean> bandClassExists = client.schema().exists().withClassName(clazz.getClassName()).run();
    Result<Boolean> nonExistentClassExists = client.schema().exists().withClassName("nonExistentClass").run();
    // then
    assertResultTrue(createStatus);
    assertResultTrue(bandClassExists);
    assertNotNull(nonExistentClassExists);
    assertFalse(nonExistentClassExists.getResult());
    assertNull(nonExistentClassExists.getError());
    Result<Shard[]> shards = client.schema().shardsGetter()
      .withClassName(clazz.getClassName()).run();
    assertNotNull(shards);
    assertNotNull(shards.getResult());
    assertEquals(1, shards.getResult().length);
    Shard shard = shards.getResult()[0];
    assertNotNull(shard.getName());
    assertNotNull(shard.getStatus());
  }

  @Test
  public void testSchemaUpdateShard() {
    // given
    String className = "Band";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertNull(createStatus.getError());
    assertTrue(createStatus.getResult());
    // then
    Result<Shard[]> shards = client.schema().shardsGetter().withClassName(className).run();
    assertNotNull(shards);
    assertNull(shards.getError());
    assertNotNull(shards.getResult());
    assertEquals(1, shards.getResult().length);
    // check the shard status, should be READY
    assertEquals(ShardStatuses.READY, shards.getResult()[0].getStatus());
    // get shard's name
    String shardName = shards.getResult()[0].getName();
    assertNotNull(shardName);
    // update shard status to READONLY
    Result<ShardStatus> updateToREADONLY = client.schema().shardUpdater()
      .withClassName(className)
      .withShardName(shardName)
      .withStatus(ShardStatuses.READONLY)
      .run();
    assertNotNull(updateToREADONLY.getResult());
    assertEquals(ShardStatuses.READONLY, updateToREADONLY.getResult().getStatus());
    // update shard status to READY
    Result<ShardStatus> updateToREADY = client.schema().shardUpdater()
      .withClassName(className)
      .withShardName(shardName)
      .withStatus(ShardStatuses.READY)
      .run();
    assertNotNull(updateToREADY.getResult());
    assertEquals(ShardStatuses.READY, updateToREADY.getResult().getStatus());
  }

  @Test
  public void testSchemaUpdateShards() {
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
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertResultTrue(createStatus);
    // then
    Result<Shard[]> shards = client.schema().shardsGetter().withClassName(className).run();
    assertNotNull(shards);
    assertNull(shards.getError());
    assertNotNull(shards.getResult());
    assertEquals(3, shards.getResult().length);
    // update shard status to READONLY
    Result<ShardStatus[]> updateToREADONLY = client.schema().shardsUpdater()
      .withClassName(className)
      .withStatus(ShardStatuses.READONLY)
      .run();
    assertNotNull(updateToREADONLY.getResult());
    assertEquals(3, updateToREADONLY.getResult().length);
    for (ShardStatus s : updateToREADONLY.getResult()) {
      assertEquals(ShardStatuses.READONLY, s.getStatus());
    }
    // update shard status to READY
    Result<ShardStatus[]> updateToREADY = client.schema().shardsUpdater()
      .withClassName(className)
      .withStatus(ShardStatuses.READY)
      .run();
    assertNotNull(updateToREADY.getResult());
    assertEquals(3, updateToREADY.getResult().length);
    for (ShardStatus s : updateToREADY.getResult()) {
      assertEquals(ShardStatuses.READY, s.getStatus());
    }
  }

  @Test
  public void testSchemaUpdateShardsException() {
    // when
    Result<ShardStatus[]> res = client.schema().shardsUpdater().run();
    Result<ShardStatus[]> res2 = client.schema().shardsUpdater().withStatus(ShardStatuses.READY).run();
    Result<ShardStatus[]> res3 = client.schema().shardsUpdater().withClassName("class").run();
    // then
    assertResultError("className, status cannot be empty", res);
    assertResultError("className cannot be empty", res2);
    assertResultError("status cannot be empty", res3);
  }

  @Test
  public void testSchemaUpdateShardException() {
    // when
    Result<ShardStatus> res = client.schema().shardUpdater().run();
    Result<ShardStatus> res2 = client.schema().shardUpdater().withStatus(ShardStatuses.READY).run();
    Result<ShardStatus> res3 = client.schema().shardUpdater().withClassName("class").run();
    Result<ShardStatus> res4 = client.schema().shardUpdater().withShardName("shardName").run();
    // then
    assertResultError("className, shardName, status cannot be empty", res);
    assertResultError("className, shardName cannot be empty", res2);
    assertResultError("shardName, status cannot be empty", res3);
    assertResultError("className, status cannot be empty", res4);
  }

  @Test
  public void testSchemaGetShardsException() {
    // when
    Result<Shard[]> res = client.schema().shardsGetter().run();
    // then
    assertResultError("className cannot be empty", res);
  }

  private void assertResultTrue(Result<Boolean> result) {
    assertNotNull(result);
    assertTrue(result.getResult());
  }

  private void assertResultError(String msg, Result<?> result) {
    assertNotNull(result);
    assertTrue(result.hasErrors());
    List<WeaviateErrorMessage> messages = result.getError().getMessages();
    assertEquals(1, messages.size());
    assertEquals(msg, messages.get(0).getMessage());
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

  @Test
  public void shouldAddObjectsWithNestedProperties_EntireSchema() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassEntireSchema(className);
    utils.createClass(client, wvtClass);

    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);

    // schema did not change after adding object 1
    WeaviateObject object1 = utils.createObject(client, utils.nestedObject1(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), object1);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);

    // schema did not change after adding object 2
    WeaviateObject object2 = utils.createObject(client, utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), object2);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_PartialSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema1(className);
    utils.createClass(client, wvtClass);

    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema did not change after adding object 1
    WeaviateObject object1 = utils.createObject(client, utils.nestedObject1(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), object1);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 2
    WeaviateObject object2 = utils.createObject(client, utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), object2);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_PartialSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema2(className);
    utils.createClass(client, wvtClass);

    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema did not change after adding object 2
    WeaviateObject object2 = utils.createObject(client, utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), object2);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 1
    WeaviateObject object1 = utils.createObject(client, utils.nestedObject1(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), object1);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_NoSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding object 1
    WeaviateObject object1 = utils.createObject(client, utils.nestedObject1(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), object1);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 2
    WeaviateObject object2 = utils.createObject(client, utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), object2);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_NoSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding object 2
    WeaviateObject object2 = utils.createObject(client, utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), object2);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 1
    WeaviateObject object1 = utils.createObject(client, utils.nestedObject1(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), object1);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_EntireSchema() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassEntireSchema(className);
    utils.createClass(client, wvtClass);

    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);

    // schema did not change after adding objects
    ObjectGetResponse[] objects = utils.batchObjects(client, utils.nestedObject1(className), utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), objects[0]);
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), objects[1]);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_PartialSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema1(className);
    utils.createClass(client, wvtClass);

    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding objects
    ObjectGetResponse[] objects = utils.batchObjects(client, utils.nestedObject1(className), utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), objects[0]);
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), objects[1]);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_PartialSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema2(className);
    utils.createClass(client, wvtClass);

    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding objects
    ObjectGetResponse[] objects = utils.batchObjects(client, utils.nestedObject1(className), utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), objects[0]);
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), objects[1]);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_NoSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding objects
    ObjectGetResponse[] objects = utils.batchObjects(client, utils.nestedObject1(className), utils.nestedObject2(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), objects[0]);
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), objects[1]);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_NoSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding objects
    ObjectGetResponse[] objects = utils.batchObjects(client, utils.nestedObject2(className), utils.nestedObject1(className));
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject2(className), objects[0]);
    utils.assertThatObjectsAreSimilar(utils.expectedNestedObject1(className), objects[1]);
    schemaClass = utils.getClass(client, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_FromFileNestedObject() throws Exception {
    // given
    File jsonFile = new File("src/test/resources/json/nested-one-object.json");
    InputStreamReader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()));
    // when
    Object nestedOneObject = new Gson().fromJson(reader, Object.class);
    String className = "ClassWithOneObjectPropertyFromFile";
    String id = "d3ca0fc9-d392-4253-8f2a-0bce51efff80";

    Map<String, Object> props = new HashMap<>();
    props.put("name", "nested object from file");
    props.put("objectProperty", nestedOneObject);

    WeaviateObject weaviateObject = WeaviateObject.builder()
      .className(className).id(id).properties(props).build();

    // then
    ObjectGetResponse[] objects = utils.batchObjects(client, weaviateObject);
    assertThat(objects).isNotEmpty();
    Result<List<WeaviateObject>> result = client.data().objectsGetter().withID(id).withClassName(className).run();
    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult)
      .isNotNull()
      .extracting(objs -> objs.get(0)).isNotNull()
      .satisfies(obj -> {
        assertThat(obj.getId()).isEqualTo(id);
        assertThat(obj.getProperties()).isNotNull()
          .extracting(p -> p.get("objectProperty")).isNotNull();
      });
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_FromFileNestedArrayObject() throws Exception {
    // given
    File jsonFile = new File("src/test/resources/json/nested-array-object.json");
    InputStreamReader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()));
    // when
    Object nestedArrayObject = new Gson().fromJson(reader, Object.class);
    String className = "ClassWithOneObjectArrayPropertyFromFile";
    String id = "d3ca0fc9-d392-4253-8f2a-0bce51efff80";

    Map<String, Object> props = new HashMap<>();
    props.put("name", "nested object from file");
    props.put("objectArrayProperty", nestedArrayObject);

    WeaviateObject weaviateObject = WeaviateObject.builder()
      .className(className).id(id).properties(props).build();

    // then
    ObjectGetResponse[] objects = utils.batchObjects(client, weaviateObject);
    assertThat(objects).isNotEmpty();
    Result<List<WeaviateObject>> result = client.data().objectsGetter().withID(id).withClassName(className).run();
    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult)
      .isNotNull()
      .extracting(objs -> objs.get(0)).isNotNull()
      .satisfies(obj -> {
        assertThat(obj.getId()).isEqualTo(id);
        assertThat(obj.getProperties()).isNotNull()
          .extracting(p -> p.get("objectArrayProperty")).isNotNull();
      });
  }

  @Test
  public void shouldUpdateClass() {
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
      .run();

    assertThat(createResult).isNotNull()
      .withFailMessage(() -> createResult.getError().toString())
      .returns(false, Result::hasErrors)
      .withFailMessage(null)
      .returns(true, Result::getResult);

    Result<WeaviateClass> createdClassResult = client.schema().classGetter()
      .withClassName(className)
      .run();

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
      .run();

    assertThat(updateResult).isNotNull()
      .withFailMessage(() -> updateResult.getError().toString())
      .returns(false, Result::hasErrors)
      .withFailMessage(null)
      .returns(true, Result::getResult);

    Result<WeaviateClass> updatedClassResult = client.schema().classGetter()
      .withClassName(className)
      .run();

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

  @Test
  public void shouldCreateClassWithVectorAndReplicationConfig() {
    Integer cleanupIntervalSeconds = 300;
    // vector index config
    Integer efConstruction = 128;
    Integer maxConnections = 64;
    Long vectorCacheMaxObjects = 500000L;
    Integer ef = -1;
    Boolean skip = false;
    Integer dynamicEfFactor = 8;
    Integer dynamicEfMax = 500;
    Integer dynamicEfMin = 100;
    Integer flatSearchCutoff = 40000;
    String distance = DistanceType.DOT;
    //pq config
    Boolean enabled = true;
    Boolean bitCompression = true;
    Integer segments = 4;
    Integer centroids = 8;
    String encoderType = "tile";
    String encoderDistribution = "normal";
    // replication config
    Boolean asyncEnabled = true;
    Integer replicationFactor = 1;

    VectorIndexConfig vectorIndexConfig = VectorIndexConfig.builder()
      .cleanupIntervalSeconds(cleanupIntervalSeconds)
      .efConstruction(efConstruction)
      .maxConnections(maxConnections)
      .vectorCacheMaxObjects(vectorCacheMaxObjects)
      .ef(ef)
      .skip(skip)
      .filterStrategy(VectorIndexConfig.FilterStrategy.SWEEPING)
      .dynamicEfFactor(dynamicEfFactor)
      .dynamicEfMax(dynamicEfMax)
      .dynamicEfMin(dynamicEfMin)
      .flatSearchCutoff(flatSearchCutoff)
      .distance(distance)
      .pq(PQConfig.builder()
        .enabled(enabled)
        .bitCompression(bitCompression)
        .segments(segments)
        .centroids(centroids)
        .encoder(PQConfig.Encoder.builder()
          .type(encoderType)
          .distribution(encoderDistribution)
          .build())
        .build())
      .build();

    ReplicationConfig replicationConfig = ReplicationConfig.builder()
      .factor(replicationFactor)
      .asyncEnabled(asyncEnabled)
      .deletionStrategy(ReplicationConfig.DeletionStrategy.NO_AUTOMATED_RESOLUTION)
      .build();

    Map<String, Object> contextionaryVectorizerSettings = new HashMap<>();
    contextionaryVectorizerSettings.put("vectorizeClassName", true);
    Map<String, Object> contextionaryVectorizer = new HashMap<>();
    contextionaryVectorizer.put("text2vec-contextionary", contextionaryVectorizerSettings);

    Map<String, WeaviateClass.VectorConfig> vectorConfig = new HashMap<>();
    vectorConfig.put("hnswVector", WeaviateClass.VectorConfig.builder()
      .vectorIndexConfig(vectorIndexConfig)
      .vectorIndexType("hnsw")
      .vectorizer(contextionaryVectorizer)
      .build());

    WeaviateClass clazz = WeaviateClass.builder()
      .className("Band")
      .description("Band that plays and produces music")
      .vectorConfig(vectorConfig)
      .replicationConfig(replicationConfig)
      .build();

    Result<Boolean> createStatus = client.schema().classCreator()
      .withClass(clazz)
      .run();

    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    Result<WeaviateClass> bandClass = client.schema().classGetter()
      .withClassName(clazz.getClassName())
      .run();

    assertThat(bandClass).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(WeaviateClass::getVectorConfig)
      .satisfies(vc ->
        assertThat(vc).isNotNull()
          .containsOnlyKeys("hnswVector")
          .extracting(vcMap -> vcMap.get("hnswVector")).isNotNull()
          .satisfies(hnswVectorConfig -> {
            assertThat(hnswVectorConfig)
              .extracting(WeaviateClass.VectorConfig::getVectorIndexType)
              .isEqualTo("hnsw");

            assertThat(hnswVectorConfig)
              .extracting(WeaviateClass.VectorConfig::getVectorizer)
              .satisfies(vectorizer ->
                assertThat(vectorizer).isNotNull()
                  .containsOnlyKeys("text2vec-contextionary")
                  .extracting(vectorizerMap -> vectorizerMap.get("text2vec-contextionary")).isNotNull()
              );

            assertThat(hnswVectorConfig)
              .extracting(WeaviateClass.VectorConfig::getVectorIndexConfig)
              .returns(cleanupIntervalSeconds, VectorIndexConfig::getCleanupIntervalSeconds)
              .returns(efConstruction, VectorIndexConfig::getEfConstruction)
              .returns(maxConnections, VectorIndexConfig::getMaxConnections)
              .returns(VectorIndexConfig.FilterStrategy.SWEEPING, VectorIndexConfig::getFilterStrategy)
              .returns(vectorCacheMaxObjects, VectorIndexConfig::getVectorCacheMaxObjects)
              .returns(ef, VectorIndexConfig::getEf)
              .returns(skip, VectorIndexConfig::getSkip)
              .returns(dynamicEfFactor, VectorIndexConfig::getDynamicEfFactor)
              .returns(dynamicEfMax, VectorIndexConfig::getDynamicEfMax)
              .returns(dynamicEfMin, VectorIndexConfig::getDynamicEfMin)
              .returns(flatSearchCutoff, VectorIndexConfig::getFlatSearchCutoff)
              .returns(distance, VectorIndexConfig::getDistance)

              .extracting(VectorIndexConfig::getPq).isNotNull()
              .returns(enabled, PQConfig::getEnabled)
              .returns(bitCompression, PQConfig::getBitCompression)
              .returns(segments, PQConfig::getSegments)
              .returns(centroids, PQConfig::getCentroids)

              .extracting(PQConfig::getEncoder)
              .returns(encoderType, PQConfig.Encoder::getType)
              .returns(encoderDistribution, PQConfig.Encoder::getDistribution);
          })
      );

    assertThat(bandClass.getResult())
      .extracting(WeaviateClass::getReplicationConfig).isNotNull()
      .returns(replicationFactor, ReplicationConfig::getFactor)
      .returns(asyncEnabled, ReplicationConfig::getAsyncEnabled)
      .returns(ReplicationConfig.DeletionStrategy.NO_AUTOMATED_RESOLUTION, ReplicationConfig::getDeletionStrategy);
  }
}
