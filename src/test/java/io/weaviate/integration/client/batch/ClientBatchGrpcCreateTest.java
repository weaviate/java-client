package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.tests.batch.ClientBatchGrpcCreateTestSuite;
import java.util.List;
import java.util.function.Function;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientBatchGrpcCreateTest {

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

    Function<String, Result<Boolean>> deleteClass = (className) -> client.schema().classDeleter().withClassName(className).run();

    ClientBatchGrpcCreateTestSuite.shouldCreateBatch(client, createClass, batchCreate, fetchObject, deleteClass);
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
