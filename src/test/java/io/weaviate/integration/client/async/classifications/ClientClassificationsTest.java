package io.weaviate.integration.client.async.classifications;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.classifications.api.Scheduler;
import io.weaviate.client.v1.classifications.model.Classification;
import io.weaviate.client.v1.classifications.model.ClassificationType;
import io.weaviate.client.v1.classifications.model.ParamsKNN;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.classifications.ClassificationsTestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientClassificationsTest {

  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testClassificationScheduler() {
    String[] classifyProperties = new String[]{"tagged"};
    String[] basedOnProperties = new String[]{"description"};

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<Classification>> resultSupplier = createSupplierScheduler(
        asyncClient, scheduler -> scheduler
          .withType(ClassificationType.Contextual)
          .withClassName("Pizza")
          .withClassifyProperties(classifyProperties)
          .withBasedOnProperties(basedOnProperties)
      );
      Supplier<Result<Classification>> resultSupplierComplete = createSupplierScheduler(
        asyncClient, scheduler -> scheduler
          .withType(ClassificationType.Contextual)
          .withClassName("Pizza")
          .withClassifyProperties(classifyProperties)
          .withBasedOnProperties(basedOnProperties)
          .withWaitForCompletion()
      );

      ClassificationsTestSuite.testScheduler(resultSupplier, resultSupplierComplete, testGenerics, client);
    }
  }

  @Test
  public void testClassificationGetter() {
    String[] classifyProperties = new String[]{"tagged"};
    String[] basedOnProperties = new String[]{"description"};
    ParamsKNN paramsKNN = ParamsKNN.builder().k(3).build();

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<Classification>> resultSupplierScheduler = createSupplierScheduler(
        asyncClient, scheduler -> scheduler
          .withType(ClassificationType.KNN)
          .withClassName("Pizza")
          .withClassifyProperties(classifyProperties)
          .withBasedOnProperties(basedOnProperties)
          .withSettings(paramsKNN)
      );
      Function<String, Result<Classification>> resultSupplierGetter = createSupplierGetter(asyncClient);

      ClassificationsTestSuite.testGetter(resultSupplierScheduler, resultSupplierGetter, testGenerics, client);
    }
  }

  private Supplier<Result<Classification>> createSupplierScheduler(WeaviateAsyncClient asyncClient,
                                                                   Consumer<Scheduler> configure) {
    return () -> {
      try {
        Scheduler scheduler = asyncClient.classifications().scheduler();
        configure.accept(scheduler);
        return scheduler.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private Function<String, Result<Classification>> createSupplierGetter(WeaviateAsyncClient asyncClient) {
    return (String id) -> {
      try {
        return asyncClient.classifications().getter()
          .withID(id)
          .run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }
}
