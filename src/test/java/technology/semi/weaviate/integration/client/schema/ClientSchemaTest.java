package technology.semi.weaviate.integration.client.schema;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.schema.model.Class;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;
import technology.semi.weaviate.client.v1.schema.model.Schema;

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
  public void testSchemaCreateBandClass() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    Class clazz = Class.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .build();
    // when
    Boolean createStatus = client.schema().classCreator().withClass(clazz).run();
    Schema schema = client.schema().getter().run();
    Boolean deleteStatus = client.schema().classDeleter().withClassName(clazz.getClassName()).run();
    // then
    Assert.assertTrue(createStatus);
    Assert.assertNotNull(schema);
    Assert.assertEquals(1, schema.getClasses().size());
    Assert.assertEquals(clazz.getClassName(), schema.getClasses().get(0).getClassName());
    Assert.assertEquals(clazz.getDescription(), schema.getClasses().get(0).getDescription());
    Assert.assertTrue(deleteStatus);
  }

  @Test
  public void testSchemaCreateRunClass() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    Class clazz = Class.builder()
            .className("Run")
            .description("Running from the fuzz")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .build();
    // when
    Boolean createStatus = client.schema().classCreator().withClass(clazz).run();
    Schema schemaAfterCreate = client.schema().getter().run();
    Boolean deleteStatus = client.schema().allDeleter().run();
    Schema schemaAfterDelete = client.schema().getter().run();
    // then
    Assert.assertTrue(createStatus);
    Assert.assertNotNull(schemaAfterCreate);
    Assert.assertEquals(1, schemaAfterCreate.getClasses().size());
    Assert.assertEquals(clazz.getClassName(), schemaAfterCreate.getClasses().get(0).getClassName());
    Assert.assertEquals(clazz.getDescription(), schemaAfterCreate.getClasses().get(0).getDescription());
    Assert.assertTrue(deleteStatus);
    Assert.assertEquals(0, schemaAfterDelete.getClasses().size());
  }

  @Test
  public void testSchemaDeleteClasses() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    Class pizza = Class.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    Class chickenSoup = Class.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    // when
    Boolean pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Boolean chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Schema schemaAfterCreate = client.schema().getter().run();
    Boolean deletePizzaStatus = client.schema().classDeleter().withClassName(pizza.getClassName()).run();
    Boolean deleteChickenSoupStatus = client.schema().classDeleter().withClassName(chickenSoup.getClassName()).run();
    Schema schemaAfterDelete = client.schema().getter().run();
    // then
    Assert.assertTrue(pizzaCreateStatus);
    Assert.assertTrue(chickenSoupCreateStatus);
    Assert.assertNotNull(schemaAfterCreate);
    Assert.assertNotNull(schemaAfterCreate.getClasses());
    Assert.assertEquals(2, schemaAfterCreate.getClasses().size());
    Assert.assertEquals(pizza.getClassName(), schemaAfterCreate.getClasses().get(0).getClassName());
    Assert.assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getClasses().get(1).getDescription());
    Assert.assertTrue(deletePizzaStatus);
    Assert.assertTrue(deleteChickenSoupStatus);
    Assert.assertEquals(0, schemaAfterDelete.getClasses().size());
  }

  @Test
  public void testSchemaDeleteAllSchema() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    Class pizza = Class.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    Class chickenSoup = Class.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    // when
    Boolean pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Boolean chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Schema schemaAfterCreate = client.schema().getter().run();
    Boolean deleteAllStatus = client.schema().allDeleter().run();
    Schema schemaAfterDelete = client.schema().getter().run();
    // then
    Assert.assertTrue(pizzaCreateStatus);
    Assert.assertTrue(chickenSoupCreateStatus);
    Assert.assertNotNull(schemaAfterCreate);
    Assert.assertNotNull(schemaAfterCreate.getClasses());
    Assert.assertEquals(2, schemaAfterCreate.getClasses().size());
    Assert.assertEquals(pizza.getClassName(), schemaAfterCreate.getClasses().get(0).getClassName());
    Assert.assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getClasses().get(1).getDescription());
    Assert.assertTrue(deleteAllStatus);
    Assert.assertEquals(0, schemaAfterDelete.getClasses().size());
  }

  @Test
  public void testSchemaCreateClassesWithProperties() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    Class pizza = Class.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    Class chickenSoup = Class.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    Property newProperty = Property.builder()
            .dataType(Arrays.asList(DataType.STRING))
            .description("name")
            .name("name")
            .build();
    // when
    Boolean pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Boolean chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Boolean pizzaPropertyCreateStatus = client.schema().propertyCreator()
            .withProperty(newProperty).withClassName(pizza.getClassName()).run();
    Boolean chickenSoupPropertyCreateStatus = client.schema().propertyCreator()
            .withProperty(newProperty).withClassName(chickenSoup.getClassName()).run();
    Schema schemaAfterCreate = client.schema().getter().run();
    Boolean deleteAllStatus = client.schema().allDeleter().run();
    Schema schemaAfterDelete = client.schema().getter().run();
    // then
    Assert.assertTrue(pizzaCreateStatus);
    Assert.assertTrue(chickenSoupCreateStatus);
    Assert.assertTrue(pizzaPropertyCreateStatus);
    Assert.assertTrue(chickenSoupPropertyCreateStatus);
    Assert.assertNotNull(schemaAfterCreate);
    Assert.assertNotNull(schemaAfterCreate.getClasses());
    Assert.assertEquals(2, schemaAfterCreate.getClasses().size());
    Assert.assertEquals(pizza.getClassName(), schemaAfterCreate.getClasses().get(0).getClassName());
    Assert.assertEquals(pizza.getDescription(), schemaAfterCreate.getClasses().get(0).getDescription());
    Assert.assertNotNull(schemaAfterCreate.getClasses().get(0).getProperties());
    Assert.assertEquals(1, schemaAfterCreate.getClasses().get(0).getProperties().size());
    Assert.assertEquals(newProperty.getName(), schemaAfterCreate.getClasses().get(0).getProperties().get(0).getName());
    Assert.assertEquals(chickenSoup.getClassName(), schemaAfterCreate.getClasses().get(1).getClassName());
    Assert.assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getClasses().get(1).getDescription());
    Assert.assertNotNull(schemaAfterCreate.getClasses().get(1).getProperties());
    Assert.assertEquals(1, schemaAfterCreate.getClasses().get(1).getProperties().size());
    Assert.assertEquals(newProperty.getName(), schemaAfterCreate.getClasses().get(1).getProperties().get(0).getName());
    Assert.assertTrue(deleteAllStatus);
    Assert.assertEquals(0, schemaAfterDelete.getClasses().size());
  }
}
