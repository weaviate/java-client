package io.weaviate.integration.tests.batch;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchTestSuite {

  public static final String PIZZA_1_ID = "abefd256-8574-442b-9293-9205193737ee";
  public static final Map<String, Object> PIZZA_1_PROPS = createFoodProperties("Hawaii", "Universally accepted to be the best pizza ever created.");
  public static final String PIZZA_2_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  public static final Map<String, Object> PIZZA_2_PROPS = createFoodProperties("Doener", "A innovation, some say revolution, in the pizza industry.");
  public static final String SOUP_1_ID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
  public static final Map<String, Object> SOUP_1_PROPS = createFoodProperties("ChickenSoup", "Used by humans when their inferior genetics are attacked by " +
    "microscopic organisms.");
  public static final String SOUP_2_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
  public static final Map<String, Object> SOUP_2_PROPS = createFoodProperties("Beautiful", "Putting the game of letter soups to a whole new level.");

  public static void shouldCreateBatch(Supplier<Result<WeaviateObject>> createResPizza1, Supplier<Result<WeaviateObject>> createResSoup1,
    Function<Result<WeaviateObject>, Result<ObjectGetResponse[]>> supplyResBatchPizzas, Function<Result<WeaviateObject>, Result<ObjectGetResponse[]>> supplyResBatchSoups,
    Supplier<Result<List<WeaviateObject>>> supplyResGetPizza1, Supplier<Result<List<WeaviateObject>>> supplyResGetPizza2,
    Supplier<Result<List<WeaviateObject>>> supplyResGetSoup1, Supplier<Result<List<WeaviateObject>>> supplyResGetSoup2) {
    // when
    Result<WeaviateObject> resPizza1 = createResPizza1.get();
    Result<WeaviateObject> resSoup1 = createResSoup1.get();

    assertThat(resPizza1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();
    assertThat(resSoup1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();

    Result<ObjectGetResponse[]> resBatchPizzas = supplyResBatchPizzas.apply(resPizza1);
    Result<ObjectGetResponse[]> resBatchSoups = supplyResBatchSoups.apply(resSoup1);

    // check if created objects exist
    Result<List<WeaviateObject>> resGetPizza1 = supplyResGetPizza1.get();
    Result<List<WeaviateObject>> resGetPizza2 = supplyResGetPizza2.get();
    Result<List<WeaviateObject>> resGetSoup1 = supplyResGetSoup1.get();
    Result<List<WeaviateObject>> resGetSoup2 = supplyResGetSoup2.get();

    // then
    assertThat(resBatchPizzas).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(resBatchPizzas.getResult()).hasSize(2);

    assertThat(resBatchSoups).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(resBatchSoups.getResult()).hasSize(2);

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

  private static Map<String, Object> createFoodProperties(Object name, Object description) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", name);
    props.put("description", description);

    return props;
  }
}