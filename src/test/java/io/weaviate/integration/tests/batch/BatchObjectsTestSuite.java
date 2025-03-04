package io.weaviate.integration.tests.batch;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.WeaviateObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchObjectsTestSuite {

  public static final String PIZZA_1_ID = "abefd256-8574-442b-9293-9205193737ee";
  public static final Map<String, Object> PIZZA_1_PROPS = createFoodProperties(
      "Hawaii", "Universally accepted to be the best pizza ever created.");
  public static final String PIZZA_2_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  public static final Map<String, Object> PIZZA_2_PROPS = createFoodProperties(
      "Doener", "A innovation, some say revolution, in the pizza industry.");
  public static final String SOUP_1_ID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
  public static final Map<String, Object> SOUP_1_PROPS = createFoodProperties(
      "ChickenSoup", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
  public static final String SOUP_2_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
  public static final Map<String, Object> SOUP_2_PROPS = createFoodProperties(
      "Beautiful", "Putting the game of letter soups to a whole new level.");

  public static void testCreateBatch(Function<WeaviateObject, Result<ObjectGetResponse[]>> supplierObjectsBatcherPizzas,
      Function<WeaviateObject, Result<ObjectGetResponse[]>> supplierObjectBatcherSoups,
      Supplier<Result<WeaviateObject>> supplierDataPizza1,
      Supplier<Result<WeaviateObject>> supplierDataSoup1,
      Supplier<Result<List<WeaviateObject>>> supplierGetterPizza1,
      Supplier<Result<List<WeaviateObject>>> supplierGetterPizza2,
      Supplier<Result<List<WeaviateObject>>> supplierGetterSoup1,
      Supplier<Result<List<WeaviateObject>>> supplierGetterSoup2) {
    // when
    Result<WeaviateObject> resPizza1 = supplierDataPizza1.get();
    Result<WeaviateObject> resSoup1 = supplierDataSoup1.get();

    assertThat(resPizza1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull();
    assertThat(resSoup1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull();

    Result<ObjectGetResponse[]> resBatchPizzas = supplierObjectsBatcherPizzas.apply(resPizza1.getResult());
    Result<ObjectGetResponse[]> resBatchSoups = supplierObjectBatcherSoups.apply(resSoup1.getResult());

    assertThat(resBatchPizzas).isNotNull()
        .returns(false, Result::hasErrors);
    assertThat(resBatchPizzas.getResult()).hasSize(2);
    assertThat(resBatchSoups).isNotNull()
        .returns(false, Result::hasErrors);
    assertThat(resBatchSoups.getResult()).hasSize(2);

    // check if created objects exist
    Result<List<WeaviateObject>> resGetPizza1 = supplierGetterPizza1.get();
    Result<List<WeaviateObject>> resGetPizza2 = supplierGetterPizza2.get();
    Result<List<WeaviateObject>> resGetSoup1 = supplierGetterSoup1.get();
    Result<List<WeaviateObject>> resGetSoup2 = supplierGetterSoup2.get();

    assertThat(resGetPizza1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(PIZZA_1_ID);
    assertThat(resGetPizza2).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(PIZZA_2_ID);
    assertThat(resGetSoup1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(SOUP_1_ID);
    assertThat(resGetSoup2).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(SOUP_2_ID);
  }

  public static void testCreateAutoBatch(
      BiConsumer<WeaviateObject, Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcherPizzas,
      BiConsumer<WeaviateObject, Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcherSoups,
      Supplier<Result<WeaviateObject>> supplierDataPizza1,
      Supplier<Result<WeaviateObject>> supplierDataSoup1,
      Supplier<Result<List<WeaviateObject>>> supplierGetterPizza1,
      Supplier<Result<List<WeaviateObject>>> supplierGetterPizza2,
      Supplier<Result<List<WeaviateObject>>> supplierGetterSoup1,
      Supplier<Result<List<WeaviateObject>>> supplierGetterSoup2) {
    // when
    Result<WeaviateObject> resPizza1 = supplierDataPizza1.get();
    Result<WeaviateObject> resSoup1 = supplierDataSoup1.get();

    assertThat(resPizza1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull();
    assertThat(resSoup1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull();

    List<Result<ObjectGetResponse[]>> resBatches = Collections.synchronizedList(new ArrayList<>(2));
    supplierObjectsBatcherPizzas.accept(resPizza1.getResult(), resBatches::add);
    supplierObjectsBatcherSoups.accept(resSoup1.getResult(), resBatches::add);

    assertThat(resBatches.get(0)).isNotNull()
        .returns(false, Result::hasErrors);
    assertThat(resBatches.get(0).getResult()).hasSize(2);
    assertThat(resBatches.get(1)).isNotNull()
        .returns(false, Result::hasErrors);
    assertThat(resBatches.get(1).getResult()).hasSize(2);

    // check if created objects exist
    Result<List<WeaviateObject>> resGetPizza1 = supplierGetterPizza1.get();
    Result<List<WeaviateObject>> resGetPizza2 = supplierGetterPizza2.get();
    Result<List<WeaviateObject>> resGetSoup1 = supplierGetterSoup1.get();
    Result<List<WeaviateObject>> resGetSoup2 = supplierGetterSoup2.get();

    assertThat(resGetPizza1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(PIZZA_1_ID);
    assertThat(resGetPizza2).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(PIZZA_2_ID);
    assertThat(resGetSoup1).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(SOUP_1_ID);
    assertThat(resGetSoup2).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(1)
        .extracting(o -> ((WeaviateObject) o).getId()).first().isEqualTo(SOUP_2_ID);
  }

  public static void testCreateBatchWithPartialError(Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcherPizzas,
      Supplier<Result<List<WeaviateObject>>> supplierGetterPizza1,
      Supplier<Result<List<WeaviateObject>>> supplierGetterPizza2) {
    Result<ObjectGetResponse[]> resBatch = supplierObjectsBatcherPizzas.get();
    assertThat(resBatch).isNotNull()
        .returns(false, Result::hasErrors);
    assertThat(resBatch.getResult()).hasSize(2);

    ObjectGetResponse resPizzaWithError = resBatch.getResult()[0];
    assertThat(resPizzaWithError.getId()).isEqualTo(PIZZA_1_ID);
    assertThat(resPizzaWithError.getResult().getErrors())
        .extracting(ObjectsGetResponseAO2Result.ErrorResponse::getError).asList()
        .first()
        .extracting(i -> ((ObjectsGetResponseAO2Result.ErrorItem) i).getMessage()).asString()
        .contains("invalid text property 'name' on class 'Pizza': not a string, but json.Number");
    ObjectGetResponse resPizza = resBatch.getResult()[1];
    assertThat(resPizza.getId()).isEqualTo(PIZZA_2_ID);
    assertThat(resPizza.getResult().getErrors()).isNull();

    Result<List<WeaviateObject>> resGetPizzaWithError = supplierGetterPizza1.get();
    Result<List<WeaviateObject>> resGetPizza = supplierGetterPizza2.get();

    assertThat(resGetPizzaWithError).isNotNull()
        .returns(false, Result::hasErrors);
    assertThat(resGetPizzaWithError.getResult()).isNull();
    assertThat(resGetPizza).isNotNull()
        .returns(false, Result::hasErrors);
    assertThat(resGetPizza.getResult()).hasSize(1);
  }

  public static Map<String, Object> createFoodProperties(Object name, Object description) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", name);
    props.put("description", description);

    return props;
  }
}
