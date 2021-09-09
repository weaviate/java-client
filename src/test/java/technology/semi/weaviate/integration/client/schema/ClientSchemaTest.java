package technology.semi.weaviate.integration.client.schema;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;
import technology.semi.weaviate.client.v1.schema.model.Schema;
import technology.semi.weaviate.client.v1.schema.model.WeaviateClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ClientSchemaTest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
          new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    address = host + ":" + port;
  }

  @Test
  public void testSchemaCreateBandClass() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schema = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().classDeleter().withClassName(clazz.getClassName()).run();
    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(schema);
    assertNotNull(schema.getResult());
    assertEquals(1, schema.getResult().getClasses().size());
    assertEquals(clazz.getClassName(), schema.getResult().getClasses().get(0).getClassName());
    assertEquals(clazz.getDescription(), schema.getResult().getClasses().get(0).getDescription());
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
  }

  @Test
  public void testSchemaCreateRunClass() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Run")
            .description("Running from the fuzz")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
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

  @Test
  public void testSchemaDeleteClasses() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass pizza = WeaviateClass.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    WeaviateClass chickenSoup = WeaviateClass.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deletePizzaStatus = client.schema().classDeleter().withClassName(pizza.getClassName()).run();
    Result<Boolean> deleteChickenSoupStatus = client.schema().classDeleter().withClassName(chickenSoup.getClassName()).run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(pizzaCreateStatus);
    assertTrue(pizzaCreateStatus.getResult());
    assertNotNull(chickenSoupCreateStatus);
    assertTrue(chickenSoupCreateStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
    assertEquals(pizza.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
    assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getResult().getClasses().get(1).getDescription());
    assertNotNull(deletePizzaStatus);
    assertTrue(deletePizzaStatus.getResult());
    assertNotNull(deleteChickenSoupStatus);
    assertTrue(deleteChickenSoupStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testSchemaDeleteAllSchema() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass pizza = WeaviateClass.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    WeaviateClass chickenSoup = WeaviateClass.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(pizzaCreateStatus);
    assertTrue(pizzaCreateStatus.getResult());
    assertNotNull(chickenSoupCreateStatus);
    assertTrue(chickenSoupCreateStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
    assertEquals(pizza.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
    assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getResult().getClasses().get(1).getDescription());
    assertNotNull(deleteAllStatus);
    assertTrue(deleteAllStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testSchemaCreateClassesWithProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass pizza = WeaviateClass.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    WeaviateClass chickenSoup = WeaviateClass.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    Property newProperty = Property.builder()
            .dataType(Arrays.asList(DataType.STRING))
            .description("name")
            .name("name")
            .build();
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
    assertNotNull(pizzaCreateStatus);
    assertTrue(pizzaCreateStatus.getResult());
    assertNotNull(chickenSoupCreateStatus);
    assertTrue(chickenSoupCreateStatus.getResult());
    assertNotNull(pizzaPropertyCreateStatus);
    assertTrue(pizzaPropertyCreateStatus.getResult());
    assertNotNull(chickenSoupPropertyCreateStatus);
    assertTrue(chickenSoupPropertyCreateStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
    assertEquals(pizza.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
    assertEquals(pizza.getDescription(), schemaAfterCreate.getResult().getClasses().get(0).getDescription());
    assertNotNull(schemaAfterCreate.getResult().getClasses().get(0).getProperties());
    assertEquals(1, schemaAfterCreate.getResult().getClasses().get(0).getProperties().size());
    assertEquals(newProperty.getName(), schemaAfterCreate.getResult().getClasses().get(0).getProperties().get(0).getName());
    assertEquals(chickenSoup.getClassName(), schemaAfterCreate.getResult().getClasses().get(1).getClassName());
    assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getResult().getClasses().get(1).getDescription());
    assertNotNull(schemaAfterCreate.getResult().getClasses().get(1).getProperties());
    assertEquals(1, schemaAfterCreate.getResult().getClasses().get(1).getProperties().size());
    assertEquals(newProperty.getName(), schemaAfterCreate.getResult().getClasses().get(1).getProperties().get(0).getName());
    assertNotNull(deleteAllStatus);
    assertTrue(deleteAllStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testSchemaCreateClassWithProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Article")
            .description("A written text, for example a news article or blog post")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .properties(new ArrayList() {{
                add(Property.builder()
                      .dataType(new ArrayList(){{ add(DataType.STRING); }})
                      .description("Title of the article")
                      .name("title")
                      .build());
                add(Property.builder()
                      .dataType(new ArrayList(){{ add(DataType.TEXT); }})
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

  @Test
  public void testSchemaCreateClassWithArrayProperties() {
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
                      .dataType(new ArrayList(){{ add(DataType.STRING_ARRAY); }})
                      .name("stringArray")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList(){{ add(DataType.TEXT_ARRAY); }})
                      .name("textArray")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList(){{ add(DataType.INT_ARRAY); }})
                      .name("intArray")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList(){{ add(DataType.NUMBER_ARRAY); }})
                      .name("numberArray")
                      .build());
            }})
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertEquals(1, schemaAfterCreate.getResult().getClasses().size());
    assertEquals(clazz.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
    assertEquals(clazz.getDescription(), schemaAfterCreate.getResult().getClasses().get(0).getDescription());
    assertNotNull(schemaAfterCreate.getResult().getClasses().get(0).getProperties());
    assertEquals(4, schemaAfterCreate.getResult().getClasses().get(0).getProperties().size());
    List<Property> properties = schemaAfterCreate.getResult().getClasses().get(0).getProperties();
    for (Property prop: properties) {
      if (prop.getName() == "stringArray") {
        assertEquals(DataType.STRING_ARRAY, prop.getDataType());
      }
      if (prop.getName() == "textArray") {
        assertEquals(DataType.TEXT_ARRAY, prop.getDataType());
      }
      if (prop.getName() == "intArray") {
        assertEquals(DataType.INT_ARRAY, prop.getDataType());
      }
      if (prop.getName() == "numberArray") {
        assertEquals(DataType.NUMBER_ARRAY, prop.getDataType());
      }
    }
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testSchemaCreateClassWithAllProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Article")
            .description("A written text, for example a news article or blog post")
            .properties(new ArrayList() {{
              add(Property.builder()
                      .dataType(new ArrayList(){{ add(DataType.STRING); }})
                      .description("Title of the article")
                      .name("title")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList(){{ add(DataType.TEXT); }})
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
