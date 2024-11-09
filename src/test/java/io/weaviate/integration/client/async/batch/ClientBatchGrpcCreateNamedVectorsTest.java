package io.weaviate.integration.client.async.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.tests.batch.ClientBatchGrpcCreateNamedVectorsTestSuite;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

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

    Function<WeaviateClass, Result<Boolean>> classCreate = (weaviateClass) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.schema().classCreator()
          .withClass(weaviateClass)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    Function<WeaviateObject, Result<ObjectGetResponse[]>> batchCreate = (weaviateObj) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.batch().objectsBatcher()
          .withObjects(weaviateObj)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    Function<WeaviateObject, Result<List<WeaviateObject>>> fetch = (weaviateObject) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().objectsGetter()
          .withID(weaviateObject.getId())
          .withClassName(weaviateObject.getClassName())
          .withVector()
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    Function<String, Result<Boolean>> deleteClass = (className) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.schema().classDeleter().withClassName(className).run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    ClientBatchGrpcCreateNamedVectorsTestSuite.shouldCreateObjectsWithNamedVectors(classCreate, batchCreate, fetch, deleteClass);
  }

  private WeaviateClient createClient() {
    Config config = new Config("http", httpHost);
    config.setGRPCSecured(false);
    config.setGRPCHost(grpcHost);
    return new WeaviateClient(config);
  }
}
