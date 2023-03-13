package io.weaviate.integration.client.batch;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.integration.client.WeaviateTestGenerics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientBatchCreateTest {

  private static final String PIZZA_1_ID = "abefd256-8574-442b-9293-9205193737ee";
  private static final Map<String, Object> PIZZA_1_PROPS = createFoodProperties("Hawaii", "Universally accepted to be the best pizza ever created.");
  private static final String PIZZA_2_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  private static final Map<String, Object> PIZZA_2_PROPS = createFoodProperties("Doener", "A innovation, some say revolution, in the pizza industry.");
  private static final String SOUP_1_ID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
  private static final Map<String, Object> SOUP_1_PROPS = createFoodProperties("ChickenSoup", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
  private static final String SOUP_2_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
  private static final Map<String, Object> SOUP_2_PROPS = createFoodProperties("Beautiful", "Putting the game of letter soups to a whole new level.");

  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200))
    .withTailChildContainers(true);

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    Config config = new Config("http", host + ":" + port);

    client = new WeaviateClient(config);
    testGenerics.createWeaviateTestSchemaFood(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldCreateBatch() {
    // when
    Result<WeaviateObject> resPizza1 = client.data().creator()
      .withClassName("Pizza")
      .withID(PIZZA_1_ID)
      .withProperties(PIZZA_1_PROPS)
      .run();
    Result<WeaviateObject> resSoup1 = client.data().creator()
      .withClassName("Soup")
      .withID(SOUP_1_ID)
      .withProperties(SOUP_1_PROPS)
      .run();

    assertThat(resPizza1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();
    assertThat(resSoup1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();

    Result<ObjectGetResponse[]> resBatchPizzas = client.batch().objectsBatcher()
      .withObjects(
        resPizza1.getResult(),
        WeaviateObject.builder().className("Pizza").id(PIZZA_2_ID).properties(PIZZA_2_PROPS).build()
      )
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();
    Result<ObjectGetResponse[]> resBatchSoups = client.batch().objectsBatcher()
      .withObjects(
        resSoup1.getResult(),
        WeaviateObject.builder().className("Soup").id(SOUP_2_ID).properties(SOUP_2_PROPS).build()
      )
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .run();

    // check if created objects exist
    Result<List<WeaviateObject>> resGetPizza1 = client.data().objectsGetter().withID(PIZZA_1_ID).withClassName("Pizza").run();
    Result<List<WeaviateObject>> resGetPizza2 = client.data().objectsGetter().withID(PIZZA_2_ID).withClassName("Pizza").run();
    Result<List<WeaviateObject>> resGetSoup1 = client.data().objectsGetter().withID(SOUP_1_ID).withClassName("Soup").run();
    Result<List<WeaviateObject>> resGetSoup2 = client.data().objectsGetter().withID(SOUP_2_ID).withClassName("Soup").run();

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
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(PIZZA_1_ID);

    assertThat(resGetPizza2).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList().hasSize(1)
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(PIZZA_2_ID);

    assertThat(resGetSoup1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList().hasSize(1)
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(SOUP_1_ID);

    assertThat(resGetSoup2).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList().hasSize(1)
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(SOUP_2_ID);
  }

  @Test
  public void shouldCreateAutoBatch() {
    // when
    Result<WeaviateObject> resPizza1 = client.data().creator()
      .withClassName("Pizza")
      .withID(PIZZA_1_ID)
      .withProperties(PIZZA_1_PROPS)
      .run();
    Result<WeaviateObject> resSoup1 = client.data().creator()
      .withClassName("Soup")
      .withID(SOUP_1_ID)
      .withProperties(SOUP_1_PROPS)
      .run();

    assertThat(resPizza1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();
    assertThat(resSoup1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();

    List<Result<ObjectGetResponse[]>> resBatches = Collections.synchronizedList(new ArrayList<>(2));
    ObjectsBatcher.AutoBatchConfig autoBatchConfig = ObjectsBatcher.AutoBatchConfig.defaultConfig()
      .batchSize(2)
      .callback(resBatches::add)
      .build();

    client.batch().objectsAutoBatcher(autoBatchConfig)
      .withObjects(
        resPizza1.getResult(),
        WeaviateObject.builder().className("Pizza").id(PIZZA_2_ID).properties(PIZZA_2_PROPS).build()
      ).flush();
    client.batch().objectsAutoBatcher(autoBatchConfig)
      .withObjects(
        resSoup1.getResult(),
        WeaviateObject.builder().className("Soup").id(SOUP_2_ID).properties(SOUP_2_PROPS).build()
      ).flush();

    // check if created objects exist
    Result<List<WeaviateObject>> resGetPizza1 = client.data().objectsGetter().withID(PIZZA_1_ID).withClassName("Pizza").run();
    Result<List<WeaviateObject>> resGetPizza2 = client.data().objectsGetter().withID(PIZZA_2_ID).withClassName("Pizza").run();
    Result<List<WeaviateObject>> resGetSoup1 = client.data().objectsGetter().withID(SOUP_1_ID).withClassName("Soup").run();
    Result<List<WeaviateObject>> resGetSoup2 = client.data().objectsGetter().withID(SOUP_2_ID).withClassName("Soup").run();

    // then
    assertThat(resBatches.get(0)).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(resBatches.get(0).getResult()).hasSize(2);

    assertThat(resBatches.get(1)).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(resBatches.get(1).getResult()).hasSize(2);

    assertThat(resGetPizza1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList().hasSize(1)
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(PIZZA_1_ID);

    assertThat(resGetPizza2).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList().hasSize(1)
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(PIZZA_2_ID);

    assertThat(resGetSoup1).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList().hasSize(1)
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(SOUP_1_ID);

    assertThat(resGetSoup2).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList().hasSize(1)
      .extracting(o -> ((WeaviateObject)o).getId()).first().isEqualTo(SOUP_2_ID);
  }

  @Test
  public void shouldCreateBatchWithPartialError() {
    WeaviateObject pizzaWithError = WeaviateObject.builder()
      .className("Pizza")
      .id(PIZZA_1_ID)
      .properties(createFoodProperties(1, "This pizza should throw a invalid name error"))
      .build();
    WeaviateObject pizza = WeaviateObject.builder()
      .className("Pizza")
      .id(PIZZA_2_ID)
      .properties(PIZZA_2_PROPS)
      .build();

    Result<ObjectGetResponse[]> resBatch = client.batch().objectsBatcher()
      .withObjects(pizzaWithError, pizza)
      .run();

    assertThat(resBatch).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(resBatch.getResult()).hasSize(2);

    ObjectGetResponse resPizzaWithError = resBatch.getResult()[0];
    assertThat(resPizzaWithError.getId()).isEqualTo(PIZZA_1_ID);
    assertThat(resPizzaWithError.getResult().getErrors()).isNotNull()
      .extracting(Object::toString).isEqualTo("{error=[{message=invalid string property 'name' on class 'Pizza': not a string, but json.Number}]}");
    ObjectGetResponse resPizza = resBatch.getResult()[1];
    assertThat(resPizza.getId()).isEqualTo(PIZZA_2_ID);
    assertThat(resPizza.getResult().getErrors()).isNull();

    Result<List<WeaviateObject>> resGetPizzaWithError = client.data().objectsGetter()
      .withClassName("Pizza")
      .withID(PIZZA_1_ID)
      .run();
    Result<List<WeaviateObject>> resGetPizza = client.data().objectsGetter()
      .withClassName("Pizza")
      .withID(PIZZA_2_ID)
      .run();

    assertThat(resGetPizzaWithError).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(resGetPizzaWithError.getResult()).isNull();

    assertThat(resGetPizza).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(resGetPizza.getResult()).hasSize(1);
  }


  private static Map<String, Object> createFoodProperties(Object name, Object description) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", name);
    props.put("description", description);

    return props;
  }
}
