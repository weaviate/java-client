package io.weaviate.integration.tests.batch;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchReferencesMockServerTestSuite {
  public static final String PIZZA_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  public static final String SOUP_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
  public static final String FROM_PIZZA = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", PIZZA_ID);
  public static final String FROM_SOUP = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", SOUP_ID);
  public static final String TO_PIZZA = String.format("weaviate://localhost/%s/%s", "Pizza", PIZZA_ID);
  public static final String TO_SOUP = String.format("weaviate://localhost/%s/%s", "Soup", SOUP_ID);


  public static void testNotCreateBatchReferencesDueToConnectionIssue(Supplier<Result<BatchReferenceResponse[]>> supplierReferencesBatcher,
                                                                      long execMin, long execMax) {
    ZonedDateTime start = ZonedDateTime.now();
    Result<BatchReferenceResponse[]> resReferences = supplierReferencesBatcher.get();
    ZonedDateTime end = ZonedDateTime.now();

    assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(execMin, execMax);
    assertThat(resReferences.getResult()).isNull();
    assertThat(resReferences.hasErrors()).isTrue();

    List<WeaviateErrorMessage> errorMessages = resReferences.getError().getMessages();
    assertThat(errorMessages).hasSize(2);
    assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(ConnectException.class);
    assertThat(errorMessages.get(0).getMessage()).contains("Connection refused");
    assertThat(errorMessages.get(1).getThrowable()).isNull();
    assertThat(errorMessages.get(1).getMessage()).contains(
      FROM_PIZZA + " => " + TO_SOUP,
      FROM_SOUP + " => " + TO_PIZZA,
      FROM_PIZZA + " => " + TO_PIZZA,
      FROM_SOUP + " => " + TO_SOUP
    );
  }

  public static void testNotCreateAutoBatchReferencesDueToConnectionIssue(Consumer<Consumer<Result<BatchReferenceResponse[]>>> supplierReferencesBatcher,
                                                                          long execMin, long execMax) {
    List<Result<BatchReferenceResponse[]>> resultsReferences = Collections.synchronizedList(new ArrayList<>(2));

    ZonedDateTime start = ZonedDateTime.now();
    supplierReferencesBatcher.accept(resultsReferences::add);
    ZonedDateTime end = ZonedDateTime.now();

    assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(execMin, execMax);
    assertThat(resultsReferences).hasSize(2);
    for (Result<BatchReferenceResponse[]> resReferences : resultsReferences) {
      assertThat(resReferences.getResult()).isNull();
      assertThat(resReferences.hasErrors()).isTrue();

      List<WeaviateErrorMessage> errorMessages = resReferences.getError().getMessages();
      assertThat(errorMessages).hasSize(2);
      assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(ConnectException.class);
      assertThat(errorMessages.get(0).getMessage()).contains("Connection refused");
      assertThat(errorMessages.get(1).getThrowable()).isNull();

      String failedRefsMessage = errorMessages.get(1).getMessage();
      if (failedRefsMessage.contains(FROM_PIZZA + " => " + TO_SOUP)) {
        assertThat(failedRefsMessage).contains(FROM_PIZZA + " => " + TO_SOUP, FROM_SOUP + " => " + TO_PIZZA);
      } else {
        assertThat(failedRefsMessage).contains(FROM_PIZZA + " => " + TO_PIZZA, FROM_SOUP + " => " + TO_SOUP);
      }
    }
  }

  public static void testNotCreateBatchReferencesDueToTimeoutIssue(Supplier<Result<BatchReferenceResponse[]>> supplierReferencesBatcher,
                                                                   Consumer<Integer> assertBatchCallsTimes,
                                                                   int expectedBatchCalls, String expectedErr) {
    Result<BatchReferenceResponse[]> resReferences = supplierReferencesBatcher.get();

    assertBatchCallsTimes.accept(expectedBatchCalls);
    assertThat(resReferences.getResult()).isNull();
    assertThat(resReferences.hasErrors()).isTrue();

    List<WeaviateErrorMessage> errorMessages = resReferences.getError().getMessages();
    assertThat(errorMessages).hasSize(2);
    assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
    assertThat(errorMessages.get(0).getMessage()).contains(expectedErr);
    assertThat(errorMessages.get(1).getThrowable()).isNull();
    assertThat(errorMessages.get(1).getMessage()).contains(
      FROM_PIZZA + " => " + TO_SOUP,
      FROM_SOUP + " => " + TO_PIZZA,
      FROM_PIZZA + " => " + TO_PIZZA,
      FROM_SOUP + " => " + TO_SOUP
    );
  }

  public static void testNotCreateAutoBatchReferencesDueToTimeoutIssue(Consumer<Consumer<Result<BatchReferenceResponse[]>>> supplierReferencesBatcher,
                                                                       Consumer<Integer> assertBatchCallsTimes,
                                                                       int expectedBatchCalls, String expectedErr) {
    List<Result<BatchReferenceResponse[]>> resultsReferences = Collections.synchronizedList(new ArrayList<>(2));

    supplierReferencesBatcher.accept(resultsReferences::add);

    assertBatchCallsTimes.accept(expectedBatchCalls * 2);
    assertThat(resultsReferences).hasSize(2);
    for (Result<BatchReferenceResponse[]> resReferences : resultsReferences) {
      assertThat(resReferences.getResult()).isNull();
      assertThat(resReferences.hasErrors()).isTrue();

      List<WeaviateErrorMessage> errorMessages = resReferences.getError().getMessages();
      assertThat(errorMessages).hasSize(2);
      assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
      assertThat(errorMessages.get(0).getMessage()).contains(expectedErr);
      assertThat(errorMessages.get(1).getThrowable()).isNull();

      String failedRefsMessage = errorMessages.get(1).getMessage();
      if (failedRefsMessage.contains(FROM_PIZZA + " => " + TO_SOUP)) {
        assertThat(failedRefsMessage).contains(FROM_PIZZA + " => " + TO_SOUP, FROM_SOUP + " => " + TO_PIZZA);
      } else {
        assertThat(failedRefsMessage).contains(FROM_PIZZA + " => " + TO_PIZZA, FROM_SOUP + " => " + TO_SOUP);
      }
    }
  }
}
