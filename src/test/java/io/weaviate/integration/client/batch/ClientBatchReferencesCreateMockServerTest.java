package io.weaviate.integration.client.batch;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.verify.VerificationTimes;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.api.ReferencesBatcher;
import io.weaviate.client.v1.batch.model.BatchReference;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(JParamsTestRunner.class)
public class ClientBatchReferencesCreateMockServerTest {
  private static final String PIZZA_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  private static final String SOUP_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";

  private WeaviateClient client;
  private ClientAndServer mockServer;
  private MockServerClient mockServerClient;

  private static final String MOCK_SERVER_HOST = "localhost";
  private static final int MOCK_SERVER_PORT = 8999;

  @Before
  public void before() {
    mockServer = startClientAndServer(MOCK_SERVER_PORT);
    mockServerClient = new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT);

    mockServerClient.when(
      request().withMethod("GET").withPath("/v1/meta")
    ).respond(
      response().withStatusCode(200).withBody(metaBody())
    );

    Config config = new Config("http", MOCK_SERVER_HOST + ":" + MOCK_SERVER_PORT, null, 1, 1, 1);
    client = new WeaviateClient(config);
  }

  @After
  public void stopMockServer() {
    mockServer.stop();
  }

  @Test
  @DataMethod(source = ClientBatchReferencesCreateMockServerTest.class,
    method = "provideForNotCreateBatchReferencesDueToConnectionIssue")
  public void shouldNotCreateBatchReferencesDueToConnectionIssue(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                                 long execMin, long execMax) {
    // stop server to simulate connection issues
    mockServer.stop();

    String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", PIZZA_ID);
    String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", SOUP_ID);
    String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", PIZZA_ID);
    String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", SOUP_ID);

    BatchReference refPizzaToSoup = BatchReference.builder().from(fromPizza).to(toSoup).build();
    BatchReference refSoupToPizza = BatchReference.builder().from(fromSoup).to(toPizza).build();
    BatchReference refPizzaToPizza = BatchReference.builder().from(fromPizza).to(toPizza).build();
    BatchReference refSoupToSoup = BatchReference.builder().from(fromSoup).to(toSoup).build();

    ZonedDateTime start = ZonedDateTime.now();
    Result<BatchReferenceResponse[]> resReferences = client.batch().referencesBatcher(batchRetriesConfig)
      .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
      .run();
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
      fromPizza + " => " + toSoup,
      fromSoup + " => " + toPizza,
      fromPizza + " => " + toPizza,
      fromSoup + " => " + toSoup
    );
  }

  @Test
  @DataMethod(source = ClientBatchReferencesCreateMockServerTest.class,
    method = "provideForNotCreateBatchReferencesDueToConnectionIssue")
  public void shouldNotCreateAutoBatchReferencesDueToConnectionIssue(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                                     long execMin, long execMax) {
    // stop server to simulate connection issues
    mockServer.stop();

    String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", PIZZA_ID);
    String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", SOUP_ID);
    String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", PIZZA_ID);
    String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", SOUP_ID);

    BatchReference refPizzaToSoup = BatchReference.builder().from(fromPizza).to(toSoup).build();
    BatchReference refSoupToPizza = BatchReference.builder().from(fromSoup).to(toPizza).build();
    BatchReference refPizzaToPizza = BatchReference.builder().from(fromPizza).to(toPizza).build();
    BatchReference refSoupToSoup = BatchReference.builder().from(fromSoup).to(toSoup).build();

    List<Result<BatchReferenceResponse[]>> resultsReferences = Collections.synchronizedList(new ArrayList<>(2));
    ReferencesBatcher.AutoBatchConfig autoBatchConfig = ReferencesBatcher.AutoBatchConfig.defaultConfig()
      .batchSize(2)
      .poolSize(1)
      .callback(resultsReferences::add)
      .build();

    ZonedDateTime start = ZonedDateTime.now();
    client.batch().referencesAutoBatcher(batchRetriesConfig, autoBatchConfig)
      .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
      .flush();
    ZonedDateTime end = ZonedDateTime.now();

    assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(execMin, execMax);
    assertThat(resultsReferences).hasSize(2);
    for (Result<BatchReferenceResponse[]> resReferences: resultsReferences) {
      assertThat(resReferences.getResult()).isNull();
      assertThat(resReferences.hasErrors()).isTrue();

      List<WeaviateErrorMessage> errorMessages = resReferences.getError().getMessages();
      assertThat(errorMessages).hasSize(2);
      assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(ConnectException.class);
      assertThat(errorMessages.get(0).getMessage()).contains("Connection refused");
      assertThat(errorMessages.get(1).getThrowable()).isNull();

      String failedRefsMessage = errorMessages.get(1).getMessage();
      if (failedRefsMessage.contains(fromPizza + " => " + toSoup)) {
        assertThat(failedRefsMessage).contains(fromPizza + " => " + toSoup, fromSoup + " => " + toPizza);
      } else {
        assertThat(failedRefsMessage).contains(fromPizza + " => " + toPizza, fromSoup + " => " + toSoup);
      }
    }
  }

  public static Object[][] provideForNotCreateBatchReferencesDueToConnectionIssue() {
    return new Object[][] {
      new Object[] {
        // final response should be available immediately
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxConnectionRetries(0)
          .build(),
        0, 100
      },
      new Object[] {
        // final response should be available after 1 retry (200 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxConnectionRetries(1)
          .build(),
        200, 300
      },
      new Object[] {
        // final response should be available after 2 retries (200 + 400 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxConnectionRetries(2)
          .build(),
        600, 700
      },
      new Object[] {
        // final response should be available after 1 retry (200 + 400 + 600 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxConnectionRetries(3)
          .build(),
        1200, 1300
      },
    };
  }

  @Test
  @DataMethod(source = ClientBatchReferencesCreateMockServerTest.class,
    method = "provideForNotCreateBatchReferencesDueToTimeoutIssue")
  public void shouldNotCreateBatchReferencesDueToTimeoutIssue(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                              int expectedBatchCalls) {
    // given client times out after 1s

    String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", PIZZA_ID);
    String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", SOUP_ID);
    String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", PIZZA_ID);
    String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", SOUP_ID);

    BatchReference refPizzaToSoup = BatchReference.builder().from(fromPizza).to(toSoup).build();
    BatchReference refSoupToPizza = BatchReference.builder().from(fromSoup).to(toPizza).build();
    BatchReference refPizzaToPizza = BatchReference.builder().from(fromPizza).to(toPizza).build();
    BatchReference refSoupToSoup = BatchReference.builder().from(fromSoup).to(toSoup).build();

    mockServerClient.when(
      request().withMethod("POST").withPath("/v1/batch/references")
    ).respond(
      response().withDelay(Delay.seconds(2)).withStatusCode(200)
    );

    Result<BatchReferenceResponse[]> resReferences = client.batch().referencesBatcher(batchRetriesConfig)
      .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
      .run();

    mockServerClient.verify(
      request().withMethod("POST").withPath("/v1/batch/references"),
      VerificationTimes.exactly(expectedBatchCalls)
    );

    assertThat(resReferences.getResult()).isNull();
    assertThat(resReferences.hasErrors()).isTrue();

    List<WeaviateErrorMessage> errorMessages = resReferences.getError().getMessages();
    assertThat(errorMessages).hasSize(2);
    assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
    assertThat(errorMessages.get(0).getMessage()).contains("Read timed out");
    assertThat(errorMessages.get(1).getThrowable()).isNull();
    assertThat(errorMessages.get(1).getMessage()).contains(
      fromPizza + " => " + toSoup,
      fromSoup + " => " + toPizza,
      fromPizza + " => " + toPizza,
      fromSoup + " => " + toSoup
    );
  }

  @Test
  @DataMethod(source = ClientBatchReferencesCreateMockServerTest.class,
    method = "provideForNotCreateBatchReferencesDueToTimeoutIssue")
  public void shouldNotCreateAutoBatchReferencesDueToTimeoutIssue(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                                  int expectedBatchCalls) {
    // given client times out after 1s

    String fromPizza = String.format("weaviate://localhost/%s/%s/otherFoods", "Pizza", PIZZA_ID);
    String fromSoup = String.format("weaviate://localhost/%s/%s/otherFoods", "Soup", SOUP_ID);
    String toPizza = String.format("weaviate://localhost/%s/%s", "Pizza", PIZZA_ID);
    String toSoup = String.format("weaviate://localhost/%s/%s", "Soup", SOUP_ID);

    BatchReference refPizzaToSoup = BatchReference.builder().from(fromPizza).to(toSoup).build();
    BatchReference refSoupToPizza = BatchReference.builder().from(fromSoup).to(toPizza).build();
    BatchReference refPizzaToPizza = BatchReference.builder().from(fromPizza).to(toPizza).build();
    BatchReference refSoupToSoup = BatchReference.builder().from(fromSoup).to(toSoup).build();

    mockServerClient.when(
      request().withMethod("POST").withPath("/v1/batch/references")
    ).respond(
      response().withDelay(Delay.seconds(2)).withStatusCode(200)
    );

    List<Result<BatchReferenceResponse[]>> resultsReferences = Collections.synchronizedList(new ArrayList<>(2));
    ReferencesBatcher.AutoBatchConfig autoBatchConfig = ReferencesBatcher.AutoBatchConfig.defaultConfig()
      .batchSize(2)
      .poolSize(1)
      .callback(resultsReferences::add)
      .build();

    client.batch().referencesAutoBatcher(batchRetriesConfig, autoBatchConfig)
      .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
      .flush();

    mockServerClient.verify(
      request().withMethod("POST").withPath("/v1/batch/references"),
      VerificationTimes.exactly(expectedBatchCalls * 2)
    );

    assertThat(resultsReferences).hasSize(2);
    for (Result<BatchReferenceResponse[]> resReferences: resultsReferences) {
      assertThat(resReferences.getResult()).isNull();
      assertThat(resReferences.hasErrors()).isTrue();

      List<WeaviateErrorMessage> errorMessages = resReferences.getError().getMessages();
      assertThat(errorMessages).hasSize(2);
      assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
      assertThat(errorMessages.get(0).getMessage()).contains("Read timed out");
      assertThat(errorMessages.get(1).getThrowable()).isNull();

      String failedRefsMessage = errorMessages.get(1).getMessage();
      if (failedRefsMessage.contains(fromPizza + " => " + toSoup)) {
        assertThat(failedRefsMessage).contains(fromPizza + " => " + toSoup, fromSoup + " => " + toPizza);
      } else {
        assertThat(failedRefsMessage).contains(fromPizza + " => " + toPizza, fromSoup + " => " + toSoup);
      }
    }
  }

  public static Object[][] provideForNotCreateBatchReferencesDueToTimeoutIssue() {
    return new Object[][] {
      new Object[] {
        // final response should be available immediately
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(0)
          .build(),
        1
      },
      new Object[] {
        // final response should be available after 1 retry (200 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(1)
          .build(),
        2
      },
      new Object[] {
        // final response should be available after 2 retries (200 + 400 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(2)
          .build(),
        3
      },
    };
  }

  private String metaBody() {
    return String.format("{\n" +
      "  \"hostname\": \"http://[::]:%s\",\n" +
      "  \"modules\": {},\n" +
      "  \"version\": \"%s\"\n" +
      "}", MOCK_SERVER_PORT, "1.17.999-mock-server-version");
  }
}
