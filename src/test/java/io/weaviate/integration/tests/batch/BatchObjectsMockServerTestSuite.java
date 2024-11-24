package io.weaviate.integration.tests.batch;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponseStatus;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.WeaviateObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchObjectsMockServerTestSuite {

  public static final String PIZZA_1_ID = "abefd256-8574-442b-9293-9205193737ee";
  private static final Map<String, Object> PIZZA_1_PROPS = createFoodProperties(
    "Hawaii", "Universally accepted to be the best pizza ever created.");
  public static final String PIZZA_2_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  private static final Map<String, Object> PIZZA_2_PROPS = createFoodProperties(
    "Doener", "A innovation, some say revolution, in the pizza industry.");
  public static final String SOUP_1_ID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
  private static final Map<String, Object> SOUP_1_PROPS = createFoodProperties(
    "ChickenSoup", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
  public static final String SOUP_2_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
  private static final Map<String, Object> SOUP_2_PROPS = createFoodProperties(
    "Beautiful", "Putting the game of letter soups to a whole new level.");

  public static final WeaviateObject PIZZA_1 = WeaviateObject.builder().className("Pizza").id(PIZZA_1_ID).properties(PIZZA_1_PROPS).build();
  public static final WeaviateObject PIZZA_2 = WeaviateObject.builder().className("Pizza").id(PIZZA_2_ID).properties(PIZZA_2_PROPS).build();
  public static final WeaviateObject SOUP_1 = WeaviateObject.builder().className("Soup").id(SOUP_1_ID).properties(SOUP_1_PROPS).build();
  public static final WeaviateObject SOUP_2 = WeaviateObject.builder().className("Soup").id(SOUP_2_ID).properties(SOUP_2_PROPS).build();


  public static void testNotCreateBatchDueToConnectionIssue(Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher,
                                                            long expectedExecMinMillis, long expectedExecMaxMillis) {
    ZonedDateTime start = ZonedDateTime.now();
    Result<ObjectGetResponse[]> resBatch = supplierObjectsBatcher.get();
    ZonedDateTime end = ZonedDateTime.now();

    assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(expectedExecMinMillis, expectedExecMaxMillis);
    assertThat(resBatch.getResult()).isNull();
    assertThat(resBatch.hasErrors()).isTrue();

    List<WeaviateErrorMessage> errorMessages = resBatch.getError().getMessages();
    assertThat(errorMessages).hasSize(2);
    assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(ConnectException.class);
    assertThat(errorMessages.get(0).getMessage()).contains("Connection refused");
    assertThat(errorMessages.get(1).getThrowable()).isNull();
    assertThat(errorMessages.get(1).getMessage()).contains(PIZZA_1_ID, PIZZA_2_ID, SOUP_1_ID, SOUP_2_ID);
  }

  public static void testNotCreateAutoBatchDueToConnectionIssue(Consumer<Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcher,
                                                                long expectedExecMinMillis, long expectedExecMaxMillis) {
    List<Result<ObjectGetResponse[]>> resBatches = Collections.synchronizedList(new ArrayList<>(2));

    ZonedDateTime start = ZonedDateTime.now();
    supplierObjectsBatcher.accept(resBatches::add);
    ZonedDateTime end = ZonedDateTime.now();

    assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(expectedExecMinMillis, expectedExecMaxMillis);
    assertThat(resBatches).hasSize(2);

    for (Result<ObjectGetResponse[]> resBatch : resBatches) {
      assertThat(resBatch.getResult()).isNull();
      assertThat(resBatch.hasErrors()).isTrue();

      List<WeaviateErrorMessage> errorMessages = resBatch.getError().getMessages();
      assertThat(errorMessages).hasSize(2);
      assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(ConnectException.class);
      assertThat(errorMessages.get(0).getMessage()).contains("Connection refused");
      assertThat(errorMessages.get(1).getThrowable()).isNull();

      String failedIdsMessage = errorMessages.get(1).getMessage();
      if (failedIdsMessage.contains(PIZZA_1_ID)) {
        assertThat(failedIdsMessage).contains(PIZZA_1_ID, PIZZA_2_ID).doesNotContain(SOUP_1_ID, SOUP_2_ID);
      } else {
        assertThat(failedIdsMessage).contains(SOUP_1_ID, SOUP_2_ID).doesNotContain(PIZZA_1_ID, PIZZA_2_ID);
      }
    }
  }

  public static void testNotCreateBatchDueToTimeoutIssue(Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher,
                                                         Consumer<Integer> assertPostObjectsCallsCount,
                                                         Consumer<Integer> assertGetPizza1CallsCount,
                                                         Consumer<Integer> assertGetPizza2CallsCount,
                                                         Consumer<Integer> assertGetSoup1CallsCount,
                                                         Consumer<Integer> assertGetSoup2CallsCount,
                                                         int expectedBatchCallsCount, String expectedErr) {
    System.out.println("shouldNotCreateBatchDueToTimeoutIssue test suite starting");

    Result<ObjectGetResponse[]> resBatch = supplierObjectsBatcher.get();

    System.out.println("shouldNotCreateBatchDueToTimeoutIssue test suite supplier get");

    assertPostObjectsCallsCount.accept(expectedBatchCallsCount);
    assertGetPizza2CallsCount.accept(expectedBatchCallsCount);
    assertGetSoup2CallsCount.accept(expectedBatchCallsCount);
    assertGetPizza1CallsCount.accept(1);
    assertGetSoup1CallsCount.accept(1);

    System.out.println("shouldNotCreateBatchDueToTimeoutIssue test suite calls count");


    assertThat(resBatch.getResult()).hasSize(2);
    assertThat(resBatch.hasErrors()).isTrue();

    System.out.println("shouldNotCreateBatchDueToTimeoutIssue test suite results 1");


    List<WeaviateErrorMessage> errorMessages = resBatch.getError().getMessages();
    assertThat(errorMessages).hasSize(2);
    assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
    assertThat(errorMessages.get(0).getMessage()).contains(expectedErr);
    assertThat(errorMessages.get(1).getThrowable()).isNull();
    assertThat(errorMessages.get(1).getMessage()).contains(PIZZA_2_ID, SOUP_2_ID).doesNotContain(PIZZA_1_ID, SOUP_1_ID);

    System.out.println("shouldNotCreateBatchDueToTimeoutIssue test suite results 2");


    assertThat(resBatch.getResult()[0])
      .returns(PIZZA_1_ID, ObjectGetResponse::getId)
      .extracting(ObjectGetResponse::getResult).isNotNull()
      .returns(ObjectGetResponseStatus.SUCCESS, ObjectsGetResponseAO2Result::getStatus)
      .returns(null, ObjectsGetResponseAO2Result::getErrors);
    assertThat(resBatch.getResult()[1])
      .returns(SOUP_1_ID, ObjectGetResponse::getId)
      .extracting(ObjectGetResponse::getResult).isNotNull()
      .returns(ObjectGetResponseStatus.SUCCESS, ObjectsGetResponseAO2Result::getStatus)
      .returns(null, ObjectsGetResponseAO2Result::getErrors);

    System.out.println("shouldNotCreateBatchDueToTimeoutIssue test suite finished");

  }

  public static void testNotCreateAutoBatchDueToTimeoutIssue(Consumer<Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcher,
                                                             Consumer<Integer> assertPostObjectsCallsCount,
                                                             Consumer<Integer> assertGetPizza1CallsCount,
                                                             Consumer<Integer> assertGetPizza2CallsCount,
                                                             Consumer<Integer> assertGetSoup1CallsCount,
                                                             Consumer<Integer> assertGetSoup2CallsCount,
                                                             int expectedBatchCallsCount, String expectedErr) {
    List<Result<ObjectGetResponse[]>> resBatches = Collections.synchronizedList(new ArrayList<>(2));
    supplierObjectsBatcher.accept(resBatches::add);

    assertPostObjectsCallsCount.accept(expectedBatchCallsCount * 2);
    assertGetPizza2CallsCount.accept(expectedBatchCallsCount);
    assertGetSoup2CallsCount.accept(expectedBatchCallsCount);
    assertGetPizza1CallsCount.accept(1);
    assertGetSoup1CallsCount.accept(1);

    assertThat(resBatches).hasSize(2);
    for (Result<ObjectGetResponse[]> resBatch : resBatches) {
      assertThat(resBatch.getResult()).hasSize(1);
      assertThat(resBatch.hasErrors()).isTrue();

      List<WeaviateErrorMessage> errorMessages = resBatch.getError().getMessages();
      assertThat(errorMessages).hasSize(2);
      assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
      assertThat(errorMessages.get(0).getMessage()).contains(expectedErr);
      assertThat(errorMessages.get(1).getThrowable()).isNull();

      String failedIdsMessage = errorMessages.get(1).getMessage();
      if (failedIdsMessage.contains(PIZZA_2_ID)) {
        assertThat(failedIdsMessage).contains(PIZZA_2_ID).doesNotContain(PIZZA_1_ID, SOUP_1_ID, SOUP_2_ID);
        assertThat(resBatch.getResult()[0])
          .returns(PIZZA_1_ID, ObjectGetResponse::getId)
          .extracting(ObjectGetResponse::getResult).isNotNull()
          .returns(ObjectGetResponseStatus.SUCCESS, ObjectsGetResponseAO2Result::getStatus)
          .returns(null, ObjectsGetResponseAO2Result::getErrors);
      } else {
        assertThat(failedIdsMessage).contains(SOUP_2_ID).doesNotContain(PIZZA_1_ID, PIZZA_2_ID, SOUP_1_ID);
        assertThat(resBatch.getResult()[0])
          .returns(SOUP_1_ID, ObjectGetResponse::getId)
          .extracting(ObjectGetResponse::getResult).isNotNull()
          .returns(ObjectGetResponseStatus.SUCCESS, ObjectsGetResponseAO2Result::getStatus)
          .returns(null, ObjectsGetResponseAO2Result::getErrors);
      }
    }
  }

  private static Map<String, Object> createFoodProperties(String name, String description) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", name);
    props.put("description", description);

    return props;
  }
}
