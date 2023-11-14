package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.io.File;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ClientBatchGrpcCreateTest {

  private static String host;
  private static Integer port;
  private static String grpcHost;
  private static Integer grpcPort;

  @ClassRule
  public static ComposeContainer compose = new ComposeContainer(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate-1", 8080, Wait.forListeningPorts(8080))
    .withExposedService("weaviate-1", 50051, Wait.forListeningPorts(50051))
    .withTailChildContainers(true);

  @Before
  public void before() {
    host = compose.getServiceHost("weaviate-1", 8080);
    port = compose.getServicePort("weaviate-1", 8080);
    grpcHost = compose.getServiceHost("weaviate-1", 50051);
    grpcPort = compose.getServicePort("weaviate-1", 50051);
  }


  @Test
  public void shouldCreateBatchUsingGRPC() {
    testCreateBatch(true);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectUsingGRPC() {
    testCreateBatchWithNested(true);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectAndNestedArrayObjectUsingGRPC() {
    testCreateBatchWithNestedAndNestArrayObject(true);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesUsingGRPC() {
    testCreateBatchWithReferenceWithoutNested(true);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesUsingGRPC() {
    testCreateBatchWithMultiReferenceWithoutNested(true);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesWithNestedPropertiesUsingGRPC() {
    testCreateBatchWithReferenceWithNested(true);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesWithNestedPropertiesUsingGRPC() {
    testCreateBatchWithMultiReferenceWithNested(true);
  }

  @Test
  public void shouldCreateBatchUsingRest() {
    testCreateBatch(false);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectUsingRest() {
    testCreateBatchWithNested(false);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectAndNestedArrayObjectUsingRest() {
    testCreateBatchWithNestedAndNestArrayObject(false);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesUsingRest() {
    testCreateBatchWithReferenceWithoutNested(false);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesUsingRest() {
    testCreateBatchWithMultiReferenceWithoutNested(false);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesWithNestedPropertiesUsingRest() {
    testCreateBatchWithReferenceWithNested(false);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesWithNestedPropertiesUsingRest() {
    testCreateBatchWithMultiReferenceWithNested(false);
  }

  private void testCreateBatchWithReferenceWithoutNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithCrossReference();
    WeaviateObject[] objects = testData.objectsWithCrossReferences();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatchWithMultiReferenceWithoutNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithMultiCrossReference();
    WeaviateObject[] objects = testData.objectsWithMultiCrossReferences();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatchWithReferenceWithNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithCrossReferenceWithNestedProperties();
    WeaviateObject[] objects = testData.objectsWithCrossReferencesWithNestedProperties();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatchWithMultiReferenceWithNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithMultiCrossReferenceWithNestedProperties();
    WeaviateObject[] objects = testData.objectsWithMultiCrossReferencesWithNestedProperties();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatch(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.properties();
    WeaviateObject[] objects = testData.objects();
    testCreateBatch(client, className, properties, objects);
  }

  private void testCreateBatchWithNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithNestedObject();
    WeaviateObject[] objects = testData.objectsWithNestedObject();
    testCreateBatch(client, className, properties, objects);
  }

  private void testCreateBatchWithNestedAndNestArrayObject(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithNestedObjectAndNestedArrayObject();
    WeaviateObject[] objects = testData.objectsWithNestedObjectAndNestedArrayObject();
    testCreateBatch(client, className, properties, objects);
  }

  private WeaviateClient createClient(Boolean useGRPC) {
    Config config = new Config("http", host + ":" + port);
    if (useGRPC) {
      config.setGRPCSecured(false);
      config.setGRPCHost(grpcHost + ":" + grpcPort);
    }
    return new WeaviateClient(config);
  }

  private void testCreateBatch(WeaviateClient client, String className, List<Property> properties, WeaviateObject[] objects) {
    // create schema
    Result<Boolean> createResult = client.schema().classCreator()
      .withClass(WeaviateClass.builder()
        .className(className)
        .properties(properties)
        .build()
      )
      .run();
    assertThat(createResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    Result<ObjectGetResponse[]> result = client.batch().objectsBatcher()
      .withObjects(objects)
      .run();
    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(objects.length);

    for (WeaviateObject obj : objects) {
      Result<List<WeaviateObject>> resultObj = client.data().objectsGetter()
        .withID(obj.getId()).withClassName(obj.getClassName())
        .run();
      assertThat(resultObj).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull()
        .extracting(r -> r.get(0)).isNotNull()
        .satisfies(o -> {
          assertThat(o.getId()).isEqualTo(obj.getId());
          assertThat(o.getProperties()).isNotNull()
            .extracting(Map::size).isEqualTo(obj.getProperties().size());
          obj.getProperties().keySet().stream().forEach(propName -> {
            assertThat(o.getProperties().get(propName)).isNotNull();
          });
        });
    }
    // clean up
    Result<Boolean> delete = client.schema().classDeleter().withClassName(className).run();
    assertThat(delete).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isEqualTo(Boolean.TRUE);
  }
}
