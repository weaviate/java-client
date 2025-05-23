package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.batch.BatchObjectsTestSuite;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientBatchCreateTest {

  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
    testGenerics.createWeaviateTestSchemaFood(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldCreateBatch() {
    Function<WeaviateObject, Result<ObjectGetResponse[]>> supplierObjectsBatcherPizzas = pizza -> client.batch()
        .objectsBatcher()
        .withObjects(pizza, WeaviateObject.builder()
            .className("Pizza")
            .id(BatchObjectsTestSuite.PIZZA_2_ID)
            .properties(BatchObjectsTestSuite.PIZZA_2_PROPS)
            .build())
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run();
    Function<WeaviateObject, Result<ObjectGetResponse[]>> supplierObjectsBatcherSoups = soup -> client.batch()
        .objectsBatcher()
        .withObjects(soup, WeaviateObject.builder()
            .className("Soup")
            .id(BatchObjectsTestSuite.SOUP_2_ID)
            .properties(BatchObjectsTestSuite.SOUP_2_PROPS)
            .build())
        .withConsistencyLevel(ConsistencyLevel.QUORUM)
        .run();

    BatchObjectsTestSuite.testCreateBatch(supplierObjectsBatcherPizzas, supplierObjectsBatcherSoups,
        createSupplierDataPizza1(), createSupplierDataSoup1(),
        createSupplierGetterPizza1(), createSupplierGetterPizza2(),
        createSupplierGetterSoup1(), createSupplierGetterSoup2());
  }

  @Test
  public void shouldCreateAutoBatch() {
    BiConsumer<WeaviateObject, Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcherPizzas = (pizza,
        callback) -> {
      ObjectsBatcher.AutoBatchConfig autoBatchConfig = ObjectsBatcher.AutoBatchConfig.defaultConfig()
          .batchSize(2)
          .callback(callback)
          .build();

      client.batch().objectsAutoBatcher(autoBatchConfig)
          .withObjects(pizza, WeaviateObject.builder().className("Pizza")
              .id(BatchObjectsTestSuite.PIZZA_2_ID)
              .properties(BatchObjectsTestSuite.PIZZA_2_PROPS)
              .build())
          .flush();
    };
    BiConsumer<WeaviateObject, Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcherSoups = (soup,
        callback) -> {
      ObjectsBatcher.AutoBatchConfig autoBatchConfig = ObjectsBatcher.AutoBatchConfig.defaultConfig()
          .batchSize(2)
          .callback(callback)
          .build();

      client.batch().objectsAutoBatcher(autoBatchConfig)
          .withObjects(soup, WeaviateObject.builder()
              .className("Soup")
              .id(BatchObjectsTestSuite.SOUP_2_ID)
              .properties(BatchObjectsTestSuite.SOUP_2_PROPS)
              .build())
          .flush();
    };

    BatchObjectsTestSuite.testCreateAutoBatch(supplierObjectsBatcherPizzas, supplierObjectsBatcherSoups,
        createSupplierDataPizza1(), createSupplierDataSoup1(),
        createSupplierGetterPizza1(), createSupplierGetterPizza2(),
        createSupplierGetterSoup1(), createSupplierGetterSoup2());
  }

  @Test
  public void shouldCreateBatchWithPartialError() {
    Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcherPizzas = () -> {
      WeaviateObject pizzaWithError = WeaviateObject.builder()
          .className("Pizza")
          .id(BatchObjectsTestSuite.PIZZA_1_ID)
          .properties(BatchObjectsTestSuite.createFoodProperties(1, "This pizza should throw a invalid name error"))
          .build();
      WeaviateObject pizza = WeaviateObject.builder()
          .className("Pizza")
          .id(BatchObjectsTestSuite.PIZZA_2_ID)
          .properties(BatchObjectsTestSuite.PIZZA_2_PROPS)
          .build();

      return client.batch().objectsBatcher()
          .withObjects(pizzaWithError, pizza)
          .run();
    };

    BatchObjectsTestSuite.testCreateBatchWithPartialError(supplierObjectsBatcherPizzas,
        createSupplierGetterPizza1(), createSupplierGetterPizza2());
  }

  @NotNull
  private Supplier<Result<WeaviateObject>> createSupplierDataSoup1() {
    return () -> client.data().creator()
        .withClassName("Soup")
        .withID(BatchObjectsTestSuite.SOUP_1_ID)
        .withProperties(BatchObjectsTestSuite.SOUP_1_PROPS)
        .run();
  }

  @NotNull
  private Supplier<Result<WeaviateObject>> createSupplierDataPizza1() {
    return () -> client.data().creator()
        .withClassName("Pizza")
        .withID(BatchObjectsTestSuite.PIZZA_1_ID)
        .withProperties(BatchObjectsTestSuite.PIZZA_1_PROPS)
        .run();
  }

  @NotNull
  private Supplier<Result<List<WeaviateObject>>> createSupplierGetterPizza1() {
    return () -> client.data().objectsGetter()
        .withID(BatchObjectsTestSuite.PIZZA_1_ID)
        .withClassName("Pizza")
        .run();
  }

  @NotNull
  private Supplier<Result<List<WeaviateObject>>> createSupplierGetterPizza2() {
    return () -> client.data().objectsGetter()
        .withID(BatchObjectsTestSuite.PIZZA_2_ID)
        .withClassName("Pizza")
        .run();
  }

  @NotNull
  private Supplier<Result<List<WeaviateObject>>> createSupplierGetterSoup1() {
    return () -> client.data().objectsGetter()
        .withID(BatchObjectsTestSuite.SOUP_1_ID)
        .withClassName("Soup")
        .run();
  }

  @NotNull
  private Supplier<Result<List<WeaviateObject>>> createSupplierGetterSoup2() {
    return () -> client.data().objectsGetter()
        .withID(BatchObjectsTestSuite.SOUP_2_ID)
        .withClassName("Soup")
        .run();
  }
}
