package io.weaviate.integration.tests.batch;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchReferencesTestSuite {
  public static final String PIZZA_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  public static final Map<String, Object> PIZZA_PROPS = createFoodProperties("Doener",
    "A innovation, some say revolution, in the pizza industry.");
  public static final String SOUP_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
  public static final Map<String, Object> SOUP_PROPS = createFoodProperties("Beautiful",
    "Putting the game of letter soups to a whole new level.");

  public static void testCreateBatchReferences(Supplier<Result<BatchReferenceResponse[]>> supplierReferencesBatcher,
                                               Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher,
                                               Supplier<Result<List<WeaviateObject>>> supplierGetterPizza,
                                               Supplier<Result<List<WeaviateObject>>> supplierGetterSoup) {
    // given
    Result<ObjectGetResponse[]> batchResult = supplierObjectsBatcher.get();
    assertThat(batchResult).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(batchResult.getResult()).hasSize(2);

    String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", PIZZA_ID);
    String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", SOUP_ID);
    String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", PIZZA_ID);
    String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", SOUP_ID);

    // when
    Result<BatchReferenceResponse[]> refsResult = supplierReferencesBatcher.get();

    // then
    assertThat(refsResult).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(refsResult.getResult()).hasSize(4);
    assertThat(refsResult.getResult())
      .extracting(BatchReferenceResponse::getFrom)
      .containsExactlyInAnyOrder(fromPizza, fromPizza, fromSoup, fromSoup);
    assertThat(refsResult.getResult())
      .extracting(BatchReferenceResponse::getTo)
      .containsExactlyInAnyOrder(toPizza, toPizza, toSoup, toSoup);

    Result<List<WeaviateObject>> pizzaResult = supplierGetterPizza.get();
    assertThat(pizzaResult).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(pizzaResult.getResult()).hasSize(1);

    WeaviateObject pizza = pizzaResult.getResult().get(0);
    assertThat(pizza.getId()).isEqualTo(PIZZA_ID);
    assertThat(pizza.getProperties()).isNotNull();

    Object pizzaOtherFoods = pizza.getProperties().get("otherFoods");
    assertThat(pizzaOtherFoods).isNotNull()
      .isInstanceOf(List.class)
      .asList().hasSize(2)
      .extracting(map -> ((Map<String, String>) map).get("beacon"))
      .containsExactlyInAnyOrder(toPizza, toSoup);

    Result<List<WeaviateObject>> soupResult = supplierGetterSoup.get();
    assertThat(soupResult).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(soupResult.getResult()).hasSize(1);

    WeaviateObject soup = soupResult.getResult().get(0);
    assertThat(soup.getId()).isEqualTo(SOUP_ID);
    assertThat(soup.getProperties()).isNotNull();

    Object soupOtherFoods = soup.getProperties().get("otherFoods");
    assertThat(soupOtherFoods).isNotNull()
      .isInstanceOf(List.class)
      .asList().hasSize(2)
      .extracting(map -> ((Map<String, Object>) map).get("beacon"))
      .containsExactlyInAnyOrder(toPizza, toSoup);
  }

  public static void testCreateAutoBatchReferences(Consumer<Consumer<Result<BatchReferenceResponse[]>>> supplierReferencesBatcher,
                                                   Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher,
                                                   Supplier<Result<List<WeaviateObject>>> supplierGetterPizza,
                                                   Supplier<Result<List<WeaviateObject>>> supplierGetterSoup) {
    // given
    Result<ObjectGetResponse[]> batchResult = supplierObjectsBatcher.get();
    assertThat(batchResult).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(batchResult.getResult()).hasSize(2);

    String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", PIZZA_ID);
    String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", SOUP_ID);
    String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", PIZZA_ID);
    String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", SOUP_ID);
    List<Result<BatchReferenceResponse[]>> resultsReferences = Collections.synchronizedList(new ArrayList<>(2));

    // when
    supplierReferencesBatcher.accept(resultsReferences::add);

    // then
    assertThat(resultsReferences).hasSize(2);
    for (Result<BatchReferenceResponse[]> resReferences : resultsReferences) {
      assertThat(resReferences).isNotNull()
        .returns(false, Result::hasErrors);
      assertThat(resReferences.getResult()).hasSize(2);
      assertThat(resReferences.getResult())
        .extracting(BatchReferenceResponse::getFrom)
        .containsExactlyInAnyOrder(fromPizza, fromSoup);
      assertThat(resReferences.getResult())
        .extracting(BatchReferenceResponse::getTo)
        .containsExactlyInAnyOrder(toPizza, toSoup);
    }

    Result<List<WeaviateObject>> pizzaResult = supplierGetterPizza.get();
    assertThat(pizzaResult).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(pizzaResult.getResult()).hasSize(1);

    WeaviateObject pizza = pizzaResult.getResult().get(0);
    assertThat(pizza.getId()).isEqualTo(PIZZA_ID);
    assertThat(pizza.getProperties()).isNotNull();

    Object pizzaOtherFoods = pizza.getProperties().get("otherFoods");
    assertThat(pizzaOtherFoods).isNotNull()
      .isInstanceOf(List.class)
      .asList().hasSize(2)
      .extracting(map -> ((Map<String, String>) map).get("beacon"))
      .containsExactlyInAnyOrder(toPizza, toSoup);

    Result<List<WeaviateObject>> soupResult = supplierGetterSoup.get();
    assertThat(soupResult).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(soupResult.getResult()).hasSize(1);

    WeaviateObject soup = soupResult.getResult().get(0);
    assertThat(soup.getId()).isEqualTo(SOUP_ID);
    assertThat(soup.getProperties()).isNotNull();

    Object soupOtherFoods = soup.getProperties().get("otherFoods");
    assertThat(soupOtherFoods).isNotNull()
      .isInstanceOf(List.class)
      .asList().hasSize(2)
      .extracting(map -> ((Map<String, Object>) map).get("beacon"))
      .containsExactlyInAnyOrder(toPizza, toSoup);
  }

  private static Map<String, Object> createFoodProperties(String name, String description) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", name);
    props.put("description", description);

    return props;
  }
}
