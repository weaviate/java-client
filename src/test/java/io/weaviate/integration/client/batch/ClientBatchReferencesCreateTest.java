package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.api.ReferencesBatcher;
import io.weaviate.client.v1.batch.model.BatchReference;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.batch.BatchReferencesTestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientBatchReferencesCreateTest {

  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
    testGenerics.createWeaviateTestSchemaFoodWithReferenceProperty(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldCreateBatchReferences() {
    Supplier<Result<BatchReferenceResponse[]>> supplierReferencesBatcherResult = () -> {
      String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", BatchReferencesTestSuite.PIZZA_ID);
      String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", BatchReferencesTestSuite.SOUP_ID);
      String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", BatchReferencesTestSuite.PIZZA_ID);
      String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", BatchReferencesTestSuite.SOUP_ID);
      BatchReference refPizzaToSoup = BatchReference.builder().from(fromPizza).to(toSoup).build();
      BatchReference refSoupToPizza = BatchReference.builder().from(fromSoup).to(toPizza).build();
      BatchReference refPizzaToPizza = BatchReference.builder().from(fromPizza).to(toPizza).build();
      BatchReference refSoupToSoup = BatchReference.builder().from(fromSoup).to(toSoup).build();

      return client.batch().referencesBatcher()
        .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run();
    };

    BatchReferencesTestSuite.testCreateBatchReferences(supplierReferencesBatcherResult,
      supplierObjectsBatcher(), supplierGetterPizzaResult(), supplierGetterSoupResult());
  }

  @Test
  public void shouldCreateAutoBatchReferences() {
    Consumer<Consumer<Result<BatchReferenceResponse[]>>> supplierReferencesBatcherResult = callback -> {
      String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", BatchReferencesTestSuite.PIZZA_ID);
      String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", BatchReferencesTestSuite.SOUP_ID);
      String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", BatchReferencesTestSuite.PIZZA_ID);
      String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", BatchReferencesTestSuite.SOUP_ID);
      BatchReference refPizzaToSoup = BatchReference.builder().from(fromPizza).to(toSoup).build();
      BatchReference refSoupToPizza = BatchReference.builder().from(fromSoup).to(toPizza).build();
      BatchReference refPizzaToPizza = BatchReference.builder().from(fromPizza).to(toPizza).build();
      BatchReference refSoupToSoup = BatchReference.builder().from(fromSoup).to(toSoup).build();

      ReferencesBatcher.AutoBatchConfig autoBatchConfig = ReferencesBatcher.AutoBatchConfig.defaultConfig()
        .batchSize(2)
        .callback(callback)
        .build();

      client.batch().referencesAutoBatcher(autoBatchConfig)
        .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run();
    };

    BatchReferencesTestSuite.testCreateAutoBatchReferences(supplierReferencesBatcherResult,
      supplierObjectsBatcher(), supplierGetterPizzaResult(), supplierGetterSoupResult());
  }

  private Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher() {
    return () -> client.batch().objectsBatcher()
      .withObjects(new WeaviateObject[]{
        WeaviateObject.builder()
          .id(BatchReferencesTestSuite.PIZZA_ID)
          .className("Pizza")
          .properties(BatchReferencesTestSuite.PIZZA_PROPS)
          .build(),
        WeaviateObject.builder()
          .id(BatchReferencesTestSuite.SOUP_ID)
          .className("Soup")
          .properties(BatchReferencesTestSuite.SOUP_PROPS)
          .build()
      })
      .run();
  }

  private Supplier<Result<List<WeaviateObject>>> supplierGetterPizzaResult() {
    return () -> client.data().objectsGetter()
      .withID(BatchReferencesTestSuite.PIZZA_ID)
      .withClassName("Pizza")
      .run();
  }

  private Supplier<Result<List<WeaviateObject>>> supplierGetterSoupResult() {
    return () -> client.data().objectsGetter()
      .withID(BatchReferencesTestSuite.SOUP_ID)
      .withClassName("Soup")
      .run();
  }
}
