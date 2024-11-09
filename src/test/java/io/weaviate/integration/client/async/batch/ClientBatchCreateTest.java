package io.weaviate.integration.client.async.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.batch.BatchTestSuite;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientBatchCreateTest {
  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
    testGenerics.createWeaviateTestSchemaFood(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldCreateBatch() {
    Supplier<Result<WeaviateObject>> resPizza1 = () -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().creator()
          .withClassName("Pizza")
          .withID(BatchTestSuite.PIZZA_1_ID)
          .withProperties(BatchTestSuite.PIZZA_1_PROPS)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
    Supplier<Result<WeaviateObject>> resSoup1 = () -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().creator()
          .withClassName("Soup")
          .withID(BatchTestSuite.SOUP_1_ID)
          .withProperties(BatchTestSuite.SOUP_1_PROPS)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    Function<Result<WeaviateObject>, Result<ObjectGetResponse[]>> resBatchPizzas = (pizza1) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.batch().objectsBatcher()
          .withObjects(
            pizza1.getResult(),
            WeaviateObject.builder().className("Pizza").id(BatchTestSuite.PIZZA_2_ID).properties(BatchTestSuite.PIZZA_2_PROPS).build()
          )
          .withConsistencyLevel(ConsistencyLevel.QUORUM)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    Function<Result<WeaviateObject>, Result<ObjectGetResponse[]>> resBatchSoups = (soup1) -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.batch().objectsBatcher()
          .withObjects(
            soup1.getResult(),
            WeaviateObject.builder().className("Soup").id(BatchTestSuite.SOUP_2_ID).properties(BatchTestSuite.SOUP_2_PROPS).build()
          )
          .withConsistencyLevel(ConsistencyLevel.QUORUM)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    // check if created objects exist
    Supplier<Result<List<WeaviateObject>>> resGetPizza1 = () -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().objectsGetter().withID(BatchTestSuite.PIZZA_1_ID).withClassName("Pizza").run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
    Supplier<Result<List<WeaviateObject>>> resGetPizza2 = () -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().objectsGetter().withID(BatchTestSuite.PIZZA_2_ID).withClassName("Pizza").run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
    Supplier<Result<List<WeaviateObject>>> resGetSoup1 = () -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().objectsGetter().withID(BatchTestSuite.SOUP_1_ID).withClassName("Soup").run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
    Supplier<Result<List<WeaviateObject>>> resGetSoup2 = () -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.data().objectsGetter().withID(BatchTestSuite.SOUP_2_ID).withClassName("Soup").run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };

    BatchTestSuite.shouldCreateBatch(resPizza1, resSoup1, resBatchPizzas, resBatchSoups, resGetPizza1, resGetPizza2, resGetSoup1, resGetSoup2);
  }
}
