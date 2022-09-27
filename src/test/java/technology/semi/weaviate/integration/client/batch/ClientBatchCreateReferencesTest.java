package technology.semi.weaviate.integration.client.batch;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.model.BatchReference;
import technology.semi.weaviate.client.v1.batch.model.BatchReferenceResponse;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

public class ClientBatchCreateReferencesTest {
  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
          new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    Config config = new Config("http", host + ":" + port);

    client = new WeaviateClient(config);
    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testBatchReferences() {
    // given
    // classT
    String classTID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
    // classA
    String classAID = "07473b34-0ab2-4120-882d-303d9e13f7af";
    // references
    BatchReference refTtoA = BatchReference.builder()
            .from("weaviate://localhost/Pizza/97fa5147-bdad-4d74-9a81-f8babc811b09/otherFoods")
            .to("weaviate://localhost/Soup/07473b34-0ab2-4120-882d-303d9e13f7af")
            .build();
    BatchReference refAtoT = BatchReference.builder()
            .from("weaviate://localhost/Soup/07473b34-0ab2-4120-882d-303d9e13f7af/otherFoods")
            .to("weaviate://localhost/Pizza/97fa5147-bdad-4d74-9a81-f8babc811b09")
            .build();
    // when
    Result<WeaviateObject> classT = client.data().creator()
            .withClassName("Pizza")
            .withID(classTID)
            .withProperties(new HashMap<String, Object>() {{
              put("name", "Doener");
              put("description", "A innovation, some say revolution, in the pizza industry.");
            }})
            .run();
    Result<WeaviateObject> classA = client.data().creator()
            .withClassName("Soup")
            .withID(classAID)
            .withProperties(new HashMap<String, Object>() {{
              put("name", "Beautiful");
              put("description", "Putting the game of letter soups to a whole new level.");
            }})
            .run();
    Result<ObjectGetResponse[]> createClassT = client.batch().objectsBatcher().withObjects(classT.getResult()).run();
    Result<ObjectGetResponse[]> createClassA = client.batch().objectsBatcher().withObjects(classA.getResult()).run();
    BatchReference refTtoT = client.batch().referencePayloadBuilder()
            .withFromClassName("Pizza")
            .withFromRefProp("otherFoods")
            .withFromID(classTID)
            .withToID(classTID)
            .withToClassName("Pizza")
            .payload();
    BatchReference refAtoA = client.batch().referencePayloadBuilder()
            .withFromClassName("Soup")
            .withFromRefProp("otherFoods")
            .withFromID(classAID)
            .withToID(classAID)
            .withToClassName("Soup")
            .payload();
    Result<BatchReferenceResponse[]> refResult = client.batch().referencesBatcher()
            .withReferences(refTtoA, refTtoT, refAtoT, refAtoA)
            .run();
    Result<List<WeaviateObject>> objT = client.data().objectsGetter().withID(classTID).withClassName("Pizza").run();
    Result<List<WeaviateObject>> objA = client.data().objectsGetter().withID(classAID).withClassName("Soup").run();
    // then
    assertNotNull(createClassT);
    assertNotNull(createClassT.getResult());
    assertEquals(1, createClassT.getResult().length);
    assertNotNull(createClassA);
    assertNotNull(createClassA.getResult());
    assertEquals(1, createClassA.getResult().length);
    assertNotNull(refTtoT);
    assertNotNull(refAtoA);
    assertNotNull(refResult);
    assertNotNull(refResult.getResult());
    assertEquals(4, refResult.getResult().length);
    // assert objT
    assertNotNull(objT);
    assertNotNull(objT.getResult());
    assertEquals(1, objT.getResult().size());
    assertEquals(classTID, objT.getResult().get(0).getId());
    assertNotNull(classTID, objT.getResult().get(0).getProperties());
    assertNotNull(classTID, objT.getResult().get(0).getProperties().get("otherFoods"));
    Assert.assertTrue(objT.getResult().get(0).getProperties().get("otherFoods") instanceof List);
    List otherFoods = (List) objT.getResult().get(0).getProperties().get("otherFoods");
    Assert.assertEquals(2, otherFoods.size());
    Assert.assertTrue(otherFoods.get(0) instanceof Map);
    Map otherFood0 = (Map) otherFoods.get(0);
    Map otherFood1 = (Map) otherFoods.get(1);
    List beacons = Stream.of(otherFood0.get("beacon"), otherFood1.get("beacon")).collect(Collectors.toList());
    Assert.assertTrue(beacons.contains("weaviate://localhost/Soup/07473b34-0ab2-4120-882d-303d9e13f7af"));
    Assert.assertTrue(beacons.contains("weaviate://localhost/Pizza/97fa5147-bdad-4d74-9a81-f8babc811b09"));
    // assert objA
    assertNotNull(objA);
    assertNotNull(objA.getResult());
    assertEquals(1, objA.getResult().size());
    assertEquals(classAID, objA.getResult().get(0).getId());
    assertNotNull(classAID, objA.getResult().get(0).getProperties());
    assertNotNull(classAID, objA.getResult().get(0).getProperties().get("otherFoods"));
    Assert.assertTrue(objA.getResult().get(0).getProperties().get("otherFoods") instanceof List);
    otherFoods = (List) objA.getResult().get(0).getProperties().get("otherFoods");
    Assert.assertEquals(2, otherFoods.size());
    Assert.assertTrue(otherFoods.get(0) instanceof Map);
    otherFood0 = (Map) otherFoods.get(0);
    otherFood1 = (Map) otherFoods.get(1);
    beacons = Stream.of(otherFood0.get("beacon"), otherFood1.get("beacon")).collect(Collectors.toList());
    Assert.assertTrue(beacons.contains("weaviate://localhost/Soup/07473b34-0ab2-4120-882d-303d9e13f7af"));
    Assert.assertTrue(beacons.contains("weaviate://localhost/Pizza/97fa5147-bdad-4d74-9a81-f8babc811b09"));
  }
}
