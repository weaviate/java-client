package technology.semi.weaviate.integration.client.classifications;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.classifications.model.Classification;
import technology.semi.weaviate.client.v1.classifications.model.ClassificationType;
import technology.semi.weaviate.client.v1.classifications.model.ParamsKNN;
import technology.semi.weaviate.client.v1.data.model.Object;
import technology.semi.weaviate.client.v1.schema.model.Class;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ClientClassificationsTest {
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
  public void testClassificationScheduler() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String[] classifyProperties = new String[]{"tagged"};
    String[] basedOnProperties = new String[]{"description"};
    // when
    createClassificationClasses(client, testGenerics);
    Classification classification1 = client.classifications().scheduler()
            .withType(ClassificationType.Contextual)
            .withClassName("Pizza")
            .withClassifyProperties(classifyProperties)
            .withBasedOnProperties(basedOnProperties)
            .run();
    Classification classificationWithComplete = client.classifications().scheduler()
            .withType(ClassificationType.Contextual)
            .withClassName("Pizza")
            .withClassifyProperties(classifyProperties)
            .withBasedOnProperties(basedOnProperties)
            .withWaitForCompletion()
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(classification1);
    assertTrue(Arrays.asList(classification1.getBasedOnProperties()).contains("description"));
    assertTrue(Arrays.asList(classification1.getClassifyProperties()).contains("tagged"));
    assertNotNull(classificationWithComplete);
    assertTrue(Arrays.asList(classificationWithComplete.getBasedOnProperties()).contains("description"));
    assertTrue(Arrays.asList(classificationWithComplete.getClassifyProperties()).contains("tagged"));
  }

  @Test
  public void testClassificationGetter() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String[] classifyProperties = new String[]{"tagged"};
    String[] basedOnProperties = new String[]{"description"};
    ParamsKNN paramsKNN = ParamsKNN.builder().k(3).build();
    // when
    createClassificationClasses(client, testGenerics);
    Classification classification1 = client.classifications().scheduler()
            .withType(ClassificationType.KNN)
            .withClassName("Pizza")
            .withClassifyProperties(classifyProperties)
            .withBasedOnProperties(basedOnProperties)
            .withSettings(paramsKNN)
            .run();
    Classification knnClassification = client.classifications().getter().withID(classification1.getId()).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(classification1);
    assertNotNull(knnClassification);
    assertEquals(classification1.getId(), knnClassification.getId());
    assertTrue(knnClassification.getSettings() instanceof Map);
    Map settings = (Map) knnClassification.getSettings();
    assertEquals(3.0, settings.get("k"));
  }

  private void createClassificationClasses(WeaviateClient client, WeaviateTestGenerics testGenerics) {
    testGenerics.createWeaviateTestSchemaFood(client);
    // defina Tag class
    Property nameProperty = Property.builder()
            .dataType(Arrays.asList(DataType.STRING))
            .description("name")
            .name("name")
            .build();
    Class schemaClassTag = Class.builder()
            .className("Tag")
            .description("tag for a pizza")
            .properties(Stream.of(nameProperty).collect(Collectors.toList()))
            .build();
    Boolean classCreate = client.schema().classCreator().withClass(schemaClassTag).run();
    Assert.assertTrue(classCreate);
    // add tagged property
    Property tagProperty = Property.builder()
            .dataType(Arrays.asList("Tag"))
            .description("tag of pizza")
            .name("tagged")
            .build();
    Boolean addTaggedProperty = client.schema().propertyCreator().withProperty(tagProperty).withClassName("Pizza").run();
    assertTrue(addTaggedProperty);
    // create 2 pizzas
    String pizza1ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
    Object pizza1 = Object.builder().className("Pizza").id(pizza1ID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "Quattro Formaggi");
      put("description", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped " +
              "with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular " +
              "worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.");
    }}).build();
    String pizza2ID = "97fa5147-bdad-4d74-9a81-f8babc811b19";
    Object pizza2 = Object.builder().className("Pizza").id(pizza2ID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "Frutti di Mare");
      put("description", "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.");
    }}).build();
    ObjectGetResponse[] batchImport = client.batch().objectsBatcher().withObject(pizza1).withObject(pizza2).run();
    assertNotNull(batchImport);
    Assert.assertEquals(2, batchImport.length);
    // create 2 tags
    Object tag1 = Object.builder().className("Tag").properties(new HashMap<String, java.lang.Object>() {{
      put("name", "vegetarian");
    }}).build();
    Object tag2 = Object.builder().className("Tag").properties(new HashMap<String, java.lang.Object>() {{
      put("name", "seafood");
    }}).build();
    ObjectGetResponse[] batchImport2 = client.batch().objectsBatcher().withObject(tag1).withObject(tag2).run();
    assertNotNull(batchImport2);
    Assert.assertEquals(2, batchImport2.length);
  }
}
