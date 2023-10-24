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
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ClientBatchGrpcCreateTest {

  private static String host;
  private static Integer port;
  private static String grpcHost;
  private static Integer grpcPort;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200))
    .withExposedService("weaviate_1", 50051, Wait.forListeningPort())
    .withTailChildContainers(true);

  @Before
  public void before() {
    host = compose.getServiceHost("weaviate_1", 8080);
    port = compose.getServicePort("weaviate_1", 8080);
    grpcHost = compose.getServiceHost("weaviate_1", 50051);
    grpcPort = compose.getServicePort("weaviate_1", 50051);
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

  private void testCreateBatch(Boolean useGRPC) {
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.allProperties();
    WeaviateObject[] objects = testData.objects();
    testCreateBatch(useGRPC, className, properties, objects);
  }

  private void testCreateBatchWithNested(Boolean useGRPC) {
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.allPropertiesWithNestedObject();
    WeaviateObject[] objects = testData.objectsWithNestedObject();
    testCreateBatch(useGRPC, className, properties, objects);
  }

  private void testCreateBatchWithNestedAndNestArrayObject(Boolean useGRPC) {
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.allPropertiesWithNestedObjectAndNestedArrayObject();
    WeaviateObject[] objects = testData.objectsWithNestedObjectAndNestedArrayObject();
    testCreateBatch(useGRPC, className, properties, objects);
  }

  private void testCreateBatch(Boolean useGRPC, String className, List<Property> properties, WeaviateObject[] objects) {
    Config config = new Config("http", host + ":" + port);
    config.setUseGRPC(useGRPC);
    config.setGrpcAddress(grpcHost + ":" + grpcPort);
    WeaviateClient client = new WeaviateClient(config);
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
        .withID(obj.getId()).withClassName(obj.getTenant())
        .run();
      assertThat(resultObj).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull()
        .extracting(r -> r.get(0)).isNotNull()
        .satisfies(o -> {
          assertThat(o.getId()).isEqualTo(obj.getId());
          assertThat(o.getProperties()).isNotNull()
            .extracting(Map::size).isEqualTo(obj.getProperties().size());
        });
    }
    // clean up
    Result<Boolean> delete = client.schema().classDeleter().withClassName(className).run();
    assertThat(delete).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isEqualTo(Boolean.TRUE);
  }
}
