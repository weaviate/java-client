package technology.semi.weaviate.integration.client.classifications;

import java.io.File;
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
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.classifications.model.Classification;
import technology.semi.weaviate.client.v1.classifications.model.ClassificationType;
import technology.semi.weaviate.client.v1.classifications.model.ParamsKNN;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;
import technology.semi.weaviate.client.v1.schema.model.WeaviateClass;
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
  public void testClassificationScheduler() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String[] classifyProperties = new String[]{"tagged"};
    String[] basedOnProperties = new String[]{"description"};
    // when
    createClassificationClasses(client, testGenerics);
    Result<Classification> classification1 = client.classifications().scheduler()
            .withType(ClassificationType.Contextual)
            .withClassName("Pizza")
            .withClassifyProperties(classifyProperties)
            .withBasedOnProperties(basedOnProperties)
            .run();
    Result<Classification> classificationWithComplete = client.classifications().scheduler()
            .withType(ClassificationType.Contextual)
            .withClassName("Pizza")
            .withClassifyProperties(classifyProperties)
            .withBasedOnProperties(basedOnProperties)
            .withWaitForCompletion()
            .run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(classification1);
    assertNotNull(classification1.getResult());
    assertTrue(Arrays.asList(classification1.getResult().getBasedOnProperties()).contains("description"));
    assertTrue(Arrays.asList(classification1.getResult().getClassifyProperties()).contains("tagged"));
    assertNotNull(classificationWithComplete);
    assertNotNull(classificationWithComplete.getResult());
    assertTrue(Arrays.asList(classificationWithComplete.getResult().getBasedOnProperties()).contains("description"));
    assertTrue(Arrays.asList(classificationWithComplete.getResult().getClassifyProperties()).contains("tagged"));
  }

  @Test
  public void testClassificationGetter() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    String[] classifyProperties = new String[]{"tagged"};
    String[] basedOnProperties = new String[]{"description"};
    ParamsKNN paramsKNN = ParamsKNN.builder().k(3).build();
    // when
    createClassificationClasses(client, testGenerics);
    Result<Classification> classification1 = client.classifications().scheduler()
            .withType(ClassificationType.KNN)
            .withClassName("Pizza")
            .withClassifyProperties(classifyProperties)
            .withBasedOnProperties(basedOnProperties)
            .withSettings(paramsKNN)
            .run();
    Result<Classification> knnClassification = client.classifications().getter().withID(classification1.getResult().getId()).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(classification1);
    assertNotNull(classification1.getResult());
    assertNotNull(knnClassification);
    assertNotNull(knnClassification.getResult());
    assertEquals(classification1.getResult().getId(), knnClassification.getResult().getId());
    assertTrue(knnClassification.getResult().getSettings() instanceof Map);
    Map settings = (Map) knnClassification.getResult().getSettings();
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
    WeaviateClass schemaClassTag = WeaviateClass.builder()
            .className("Tag")
            .description("tag for a pizza")
            .properties(Stream.of(nameProperty).collect(Collectors.toList()))
            .build();
    Result<Boolean> classCreate = client.schema().classCreator().withClass(schemaClassTag).run();
    assertNotNull(classCreate);
    assertTrue(classCreate.getResult());
    // add tagged property
    Property tagProperty = Property.builder()
            .dataType(Arrays.asList("Tag"))
            .description("tag of pizza")
            .name("tagged")
            .build();
    Result<Boolean> addTaggedProperty = client.schema().propertyCreator().withProperty(tagProperty).withClassName("Pizza").run();
    assertNotNull(addTaggedProperty);
    assertTrue(addTaggedProperty.getResult());
    // create 2 pizzas
    String pizza1ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
    WeaviateObject pizza1 = WeaviateObject.builder().className("Pizza").id(pizza1ID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "Quattro Formaggi");
      put("description", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped " +
              "with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular " +
              "worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.");
    }}).build();
    String pizza2ID = "97fa5147-bdad-4d74-9a81-f8babc811b19";
    WeaviateObject pizza2 = WeaviateObject.builder().className("Pizza").id(pizza2ID).properties(new HashMap<String, java.lang.Object>() {{
      put("name", "Frutti di Mare");
      put("description", "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.");
    }}).build();
    Result<ObjectGetResponse[]> batchImport = client.batch().objectsBatcher().withObject(pizza1).withObject(pizza2).run();
    assertNotNull(batchImport);
    assertNotNull(batchImport.getResult());
    Assert.assertEquals(2, batchImport.getResult().length);
    // create 2 tags
    WeaviateObject tag1 = WeaviateObject.builder().className("Tag").properties(new HashMap<String, java.lang.Object>() {{
      put("name", "vegetarian");
    }}).build();
    WeaviateObject tag2 = WeaviateObject.builder().className("Tag").properties(new HashMap<String, java.lang.Object>() {{
      put("name", "seafood");
    }}).build();
    Result<ObjectGetResponse[]> batchImport2 = client.batch().objectsBatcher().withObject(tag1).withObject(tag2).run();
    assertNotNull(batchImport2);
    assertNotNull(batchImport2.getResult());
    Assert.assertEquals(2, batchImport2.getResult().length);
  }
}
