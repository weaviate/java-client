package io.weaviate.integration.client.batch;

import java.util.List;
import java.util.function.Function;

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
import io.weaviate.integration.tests.batch.ClientBatchGrpcCreateNamedVectorsTestSuite;

public class ClientBatchGrpcCreateNamedVectorsTest {
  private static String httpHost;
  private static String grpcHost;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    httpHost = compose.getHttpHostAddress();
    grpcHost = compose.getGrpcHostAddress();
  }

  @Test
  public void shouldCreateObjectsWithNamedVectors() {
    WeaviateClient client = createClient();

    Function<WeaviateClass, Result<Boolean>> classCreate = (weaviateClass) -> client.schema().classCreator()
        .withClass(weaviateClass)
        .run();

    Function<WeaviateObject, Result<ObjectGetResponse[]>> batchCreate = (weaviateObj) -> client.batch().objectsBatcher()
        .withObjects(weaviateObj)
        .run();

    Function<WeaviateObject, Result<List<WeaviateObject>>> fetch = (weaviateObject) -> client.data().objectsGetter()
        .withID(weaviateObject.getId())
        .withClassName(weaviateObject.getClassName())
        .withVector()
        .run();

    Function<String, Result<Boolean>> deleteClass = (className) -> client.schema().classDeleter()
        .withClassName(className).run();

    ClientBatchGrpcCreateNamedVectorsTestSuite.shouldCreateObjectsWithNamedVectors(classCreate, batchCreate, fetch,
        deleteClass);
  }

  @Test
  public void shouldCreateObjectsWithNamedMultiVectors() {
    WeaviateClient client = createClient();

    Function<WeaviateClass, Result<Boolean>> classCreate = (weaviateClass) -> client.schema().classCreator()
        .withClass(weaviateClass)
        .run();

    Function<WeaviateObject, Result<ObjectGetResponse[]>> batchCreate = (weaviateObj) -> client.batch().objectsBatcher()
        .withObjects(weaviateObj)
        .run();

    Function<WeaviateObject, Result<List<WeaviateObject>>> fetch = (weaviateObject) -> client.data().objectsGetter()
        .withID(weaviateObject.getId())
        .withClassName(weaviateObject.getClassName())
        .withVector()
        .run();

    Function<String, Result<Boolean>> deleteClass = (className) -> client.schema().classDeleter()
        .withClassName(className).run();

    ClientBatchGrpcCreateNamedVectorsTestSuite.shouldCreateObjectsWithNamedMultiVectors(classCreate, batchCreate, fetch,
        deleteClass);
  }

  private WeaviateClient createClient() {
    Config config = new Config("http", httpHost);
    config.setGRPCSecured(false);
    config.setGRPCHost(grpcHost);
    return new WeaviateClient(config);
  }
}
