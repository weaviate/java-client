package io.weaviate.integration.client.classifications;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
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

    Supplier<Result<Classification>> resultSupplier = () -> client.classifications().scheduler()
      .withType(ClassificationType.Contextual)
      .withClassName("Pizza")
      .withClassifyProperties(classifyProperties)
      .withBasedOnProperties(basedOnProperties)
      .run();
    Supplier<Result<Classification>> resultSupplierComplete = () -> client.classifications().scheduler()
      .withType(ClassificationType.Contextual)
      .withClassName("Pizza")
      .withClassifyProperties(classifyProperties)
      .withBasedOnProperties(basedOnProperties)
      .withWaitForCompletion()
      .run();

    ClassificationsTestSuite.testScheduler(resultSupplier, resultSupplierComplete, testGenerics, client);
  }

  @Test
  public void testClassificationGetter() {
    String[] classifyProperties = new String[]{"tagged"};
    String[] basedOnProperties = new String[]{"description"};
    ParamsKNN paramsKNN = ParamsKNN.builder().k(3).build();

    Supplier<Result<Classification>> resultSupplierScheduler = () -> client.classifications().scheduler()
      .withType(ClassificationType.KNN)
      .withClassName("Pizza")
      .withClassifyProperties(classifyProperties)
      .withBasedOnProperties(basedOnProperties)
      .withSettings(paramsKNN)
      .run();
    Function<String, Result<Classification>> resultSupplierGetter = (String id) -> client.classifications().getter()
      .withID(id)
      .run();

    ClassificationsTestSuite.testGetter(resultSupplierScheduler, resultSupplierGetter, testGenerics, client);
  }
}
