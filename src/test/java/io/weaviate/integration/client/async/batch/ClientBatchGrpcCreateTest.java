package io.weaviate.integration.client.async.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.tests.batch.ClientBatchGrpcCreateTestSuite;
import java.util.List;
import java.util.concurrent.ExecutionException;
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

    Function<WeaviateClass, Result<Boolean>> createClass = (weaviateClass) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.schema().classCreator()
          .withClass(weaviateClass)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    Function<WeaviateObject[], Result<ObjectGetResponse[]>> batchCreate = (objects) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.batch().objectsBatcher()
          .withObjects(objects)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    Function<WeaviateObject, Result<List<WeaviateObject>>> fetchObject = (obj) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().objectsGetter()
          .withID(obj.getId()).withClassName(obj.getClassName()).withVector()
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
