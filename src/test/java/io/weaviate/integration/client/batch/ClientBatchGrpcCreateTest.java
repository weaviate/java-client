package io.weaviate.integration.client.batch;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.batch.BatchObjectsTestSuite;
import io.weaviate.integration.tests.batch.ClientBatchGrpcCreateTestSuite;

public class ClientBatchGrpcCreateTest {

  private static String httpHost;
  private static String grpcHost;

  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    httpHost = compose.getHttpHostAddress();
    grpcHost = compose.getGrpcHostAddress();

    WeaviateClient client = createClient(false);

    testGenerics.cleanupWeaviate(client);
    testGenerics.createWeaviateTestSchemaFood(client);
  }

  @Test
  public void shouldCreateGRPC() {
    shouldCreate(true);
  }

  @Test
  public void shouldCreateWithoutGRPC() {
    shouldCreate(false);
  }

  public void shouldCreate(boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);

    Function<WeaviateClass, Result<Boolean>> createClass = (weaviateClass) -> client.schema().classCreator()
        .withClass(weaviateClass)
        .run();

    Function<WeaviateObject[], Result<ObjectGetResponse[]>> batchCreate = (objects) -> client.batch().objectsBatcher()
        .withObjects(objects)
        .run();

    Function<WeaviateObject, Result<List<WeaviateObject>>> fetchObject = (obj) -> client.data().objectsGetter()
        .withID(obj.getId()).withClassName(obj.getClassName()).withVector()
        .run();

    Function<String, Result<Boolean>> deleteClass = (className) -> client.schema().classDeleter()
        .withClassName(className).run();

    ClientBatchGrpcCreateTestSuite.shouldCreateBatch(client, createClass, batchCreate, fetchObject, deleteClass);
  }

  @Test
  public void testPartialErrorResponse() {
    WeaviateClient client = createClient(true);

    WeaviateObject[] batchObjects = {
        WeaviateObject.builder()
            .className("Pizza")
            .id(UUID.randomUUID().toString())
            .properties(BatchObjectsTestSuite.createFoodProperties(1, "This pizza should throw a invalid name error"))
            .build(),
        WeaviateObject.builder()
            .className("Pizza")
            .id(UUID.randomUUID().toString())
            .properties(BatchObjectsTestSuite.PIZZA_2_PROPS)
            .build(),
    };

    Result<ObjectGetResponse[]> result = client.batch().objectsBatcher()
        .withObjects(batchObjects)
        .run();

    Assertions.assertThat(result)
        .returns(true, Result::hasErrors)
        .extracting(Result::getResult).asInstanceOf(InstanceOfAssertFactories.array(ObjectGetResponse[].class))
        .hasSameSizeAs(batchObjects).as("all batch objects included in the response");

    Assertions.assertThat(result.getResult()[0].getResult().getErrors().getError().get(0).getMessage())
        .contains("invalid text property 'name' on class 'Pizza': not a string, but float64");
  }

  private WeaviateClient createClient(Boolean useGRPC) {
    Config config = new Config("http", httpHost);
    if (useGRPC) {
      config.setGRPCSecured(false);
      config.setGRPCHost(grpcHost);
    }
    return new WeaviateClient(config);
  }
}
