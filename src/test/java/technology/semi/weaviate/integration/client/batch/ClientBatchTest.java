package technology.semi.weaviate.integration.client.batch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
import technology.semi.weaviate.client.v1.batch.model.BatchReference;
import technology.semi.weaviate.client.v1.batch.model.BatchReferenceResponse;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.data.model.Object;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ClientBatchTest {
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
  public void testDataBatchCreate() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    // objT1
    String objTID = "abefd256-8574-442b-9293-9205193737ee";
    Map<String, java.lang.Object> propertiesSchemaT = new HashMap<>();
    propertiesSchemaT.put("name", "Hawaii");
    propertiesSchemaT.put("description", "Universally accepted to be the best pizza ever created.");
    // objT2
    String objT2ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
    Map<String, java.lang.Object> propertiesSchemaT2 = new HashMap<>();
    propertiesSchemaT2.put("name", "Doener");
    propertiesSchemaT2.put("description", "A innovation, some say revolution, in the pizza industry.");
    Object objT2 = Object.builder().className("Pizza").id(objT2ID).properties(propertiesSchemaT2).build();
    // objA1
    String objAID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
    Map<String, java.lang.Object> propertiesSchemaA = new HashMap<>();
    propertiesSchemaA.put("name", "ChickenSoup");
    propertiesSchemaA.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    // objA2
    String objA2ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
    Map<String, java.lang.Object> propertiesSchemaA2 = new HashMap<>();
    propertiesSchemaA2.put("name", "Beautiful");
    propertiesSchemaA2.put("description", "Putting the game of letter soups to a whole new level.");
    Object objA2 = Object.builder().className("Soup").id(objA2ID).properties(propertiesSchemaA2).build();
    // when
    testGenerics.createWeaviateTestSchemaFood(client);
    Object objT1 = client.data().creator()
            .withClassName("Pizza")
            .withID(objTID)
            .withProperties(propertiesSchemaT)
            .run();
    Object objA1 = client.data().creator()
            .withClassName("Soup")
            .withID(objAID)
            .withProperties(propertiesSchemaA)
            .run();
    ObjectGetResponse[] batchTs = client.batch().objectsBatcher()
            .withObject(objT1)
            .withObject(objT2)
            .run();
    ObjectGetResponse[] batchAs = client.batch().objectsBatcher()
            .withObject(objA1)
            .withObject(objA2)
            .run();
    List<Object> getObjT1 = client.data().objectsGetter().withID(objTID).run();
    List<Object> getObjT2 = client.data().objectsGetter().withID(objT2ID).run();
    List<Object> getObjA1 = client.data().objectsGetter().withID(objAID).run();
    List<Object> getObjA2 = client.data().objectsGetter().withID(objA2ID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(objT1);
    assertEquals(objTID, objT1.getId());
    assertNotNull(objA1);
    assertEquals(objAID, objA1.getId());
    assertNotNull(batchTs);
    assertEquals(2, batchTs.length);
    assertNotNull(batchAs);
    assertEquals(2, batchAs.length);
    assertNotNull(getObjT1);
    assertEquals(1, getObjT1.size());
    assertEquals(objTID, getObjT1.get(0).getId());
    assertNotNull(getObjT2);
    assertEquals(1, getObjT2.size());
    assertEquals(objT2ID, getObjT2.get(0).getId());
    assertNotNull(getObjA1);
    assertEquals(1, getObjA1.size());
    assertEquals(objAID, getObjA1.get(0).getId());
    assertNotNull(getObjA2);
    assertEquals(1, getObjA2.size());
    assertEquals(objA2ID, getObjA2.get(0).getId());
  }

  @Test
  public void testDataBatchReferences() throws IOException {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
    // classT
    String classTID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
    // classA
    String classAID = "07473b34-0ab2-4120-882d-303d9e13f7af";
    // references
    BatchReference refTtoA = BatchReference.builder()
            .from("weaviate://localhost/Pizza/97fa5147-bdad-4d74-9a81-f8babc811b09/otherFoods")
            .to("weaviate://localhost/07473b34-0ab2-4120-882d-303d9e13f7af")
            .build();
    BatchReference refAtoT = BatchReference.builder()
            .from("weaviate://localhost/Soup/07473b34-0ab2-4120-882d-303d9e13f7af/otherFoods")
            .to("weaviate://localhost/97fa5147-bdad-4d74-9a81-f8babc811b09")
            .build();
    // when
    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(client);
    Object classT = client.data().creator()
            .withClassName("Pizza")
            .withID(classTID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("name", "Doener");
              put("description", "A innovation, some say revolution, in the pizza industry.");
            }})
            .run();
    Object classA = client.data().creator()
            .withClassName("Soup")
            .withID(classAID)
            .withProperties(new HashMap<String, java.lang.Object>() {{
              put("name", "Beautiful");
              put("description", "Putting the game of letter soups to a whole new level.");
            }})
            .run();
    ObjectGetResponse[] createClassT = client.batch().objectsBatcher().withObject(classT).run();
    ObjectGetResponse[] createClassA = client.batch().objectsBatcher().withObject(classA).run();
    BatchReference refTtoT = client.batch().referencePayloadBuilder()
            .withFromClassName("Pizza")
            .withFromRefProp("otherFoods")
            .withFromID(classTID)
            .withToID(classTID)
            .payload();
    BatchReference refAtoA = client.batch().referencePayloadBuilder()
            .withFromClassName("Soup")
            .withFromRefProp("otherFoods")
            .withFromID(classAID)
            .withToID(classAID)
            .payload();
    BatchReferenceResponse[] refResult = client.batch().referencesBatcher()
            .withReference(refTtoA).withReference(refTtoT).withReference(refAtoT).withReference(refAtoA)
            .run();
    List<Object> objT = client.data().objectsGetter().withID(classTID).run();
    List<Object> objA = client.data().objectsGetter().withID(classAID).run();
    testGenerics.cleanupWeaviate(client);
    // then
    assertNotNull(createClassT);
    assertEquals(1, createClassT.length);
    assertNotNull(createClassA);
    assertEquals(1, createClassA.length);
    assertNotNull(refTtoT);
    assertNotNull(refAtoA);
    assertNotNull(refResult);
    assertEquals(4, refResult.length);
    // assert objT
    assertNotNull(objT);
    assertEquals(1, objT.size());
    assertEquals(classTID, objT.get(0).getId());
    assertNotNull(classTID, objT.get(0).getProperties());
    assertNotNull(classTID, objT.get(0).getProperties().get("otherFoods"));
    Assert.assertTrue(objT.get(0).getProperties().get("otherFoods") instanceof List);
    List otherFoods = (List) objT.get(0).getProperties().get("otherFoods");
    Assert.assertEquals(2, otherFoods.size());
    Assert.assertTrue(otherFoods.get(0) instanceof Map);
    Map otherFood0 = (Map) otherFoods.get(0);
    Map otherFood1 = (Map) otherFoods.get(1);
    List beacons = Stream.of(otherFood0.get("beacon"), otherFood1.get("beacon")).collect(Collectors.toList());
    Assert.assertTrue(beacons.contains("weaviate://localhost/07473b34-0ab2-4120-882d-303d9e13f7af"));
    Assert.assertTrue(beacons.contains("weaviate://localhost/97fa5147-bdad-4d74-9a81-f8babc811b09"));
    // assert objA
    assertNotNull(objA);
    assertEquals(1, objA.size());
    assertEquals(classAID, objA.get(0).getId());
    assertNotNull(classAID, objA.get(0).getProperties());
    assertNotNull(classAID, objA.get(0).getProperties().get("otherFoods"));
    Assert.assertTrue(objA.get(0).getProperties().get("otherFoods") instanceof List);
    otherFoods = (List) objA.get(0).getProperties().get("otherFoods");
    Assert.assertEquals(2, otherFoods.size());
    Assert.assertTrue(otherFoods.get(0) instanceof Map);
    otherFood0 = (Map) otherFoods.get(0);
    otherFood1 = (Map) otherFoods.get(1);
    beacons = Stream.of(otherFood0.get("beacon"), otherFood1.get("beacon")).collect(Collectors.toList());
    Assert.assertTrue(beacons.contains("weaviate://localhost/07473b34-0ab2-4120-882d-303d9e13f7af"));
    Assert.assertTrue(beacons.contains("weaviate://localhost/97fa5147-bdad-4d74-9a81-f8babc811b09"));
  }
}
