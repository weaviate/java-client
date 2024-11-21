package io.weaviate.integration.client.async.batch;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.batch.api.ReferencesBatcher;
import io.weaviate.client.v1.batch.model.BatchReference;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;
import io.weaviate.integration.tests.batch.BatchReferencesMockServerTestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.verify.VerificationTimes;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(JParamsTestRunner.class)
public class ClientBatchReferencesCreateMockServerTest {

  private WeaviateClient client;
  private ClientAndServer mockServer;
  private MockServerClient mockServerClient;

  private static final String MOCK_SERVER_HOST = "localhost";
  private static final int MOCK_SERVER_PORT = 8999;

  private static final BatchReference refPizzaToSoup = BatchReference.builder()
    .from(BatchReferencesMockServerTestSuite.FROM_PIZZA)
    .to(BatchReferencesMockServerTestSuite.TO_SOUP)
    .build();
  private static final BatchReference refSoupToPizza = BatchReference.builder()
    .from(BatchReferencesMockServerTestSuite.FROM_SOUP)
    .to(BatchReferencesMockServerTestSuite.TO_PIZZA)
    .build();
  private static final BatchReference refPizzaToPizza = BatchReference.builder()
    .from(BatchReferencesMockServerTestSuite.FROM_PIZZA)
    .to(BatchReferencesMockServerTestSuite.TO_PIZZA)
    .build();
  private static final BatchReference refSoupToSoup = BatchReference.builder()
    .from(BatchReferencesMockServerTestSuite.FROM_SOUP)
    .to(BatchReferencesMockServerTestSuite.TO_SOUP)
    .build();

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

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BatchReferenceResponse[]>> supplierReferencesBatcher = () -> {
        try {
          return asyncClient.batch().referencesBatcher(batchRetriesConfig)
            .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
            .run()
            .get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      };

      BatchReferencesMockServerTestSuite.testNotCreateBatchReferencesDueToConnectionIssue(supplierReferencesBatcher,
        execMin, execMax);
    }
  }

  @Test
  @DataMethod(source = ClientBatchReferencesCreateMockServerTest.class,
    method = "provideForNotCreateBatchReferencesDueToConnectionIssue")
  public void shouldNotCreateAutoBatchReferencesDueToConnectionIssue(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                                     long execMin, long execMax) {
    // stop server to simulate connection issues
    mockServer.stop();

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Consumer<Consumer<Result<BatchReferenceResponse[]>>> supplierReferencesBatcher = callback -> {
        ReferencesBatcher.AutoBatchConfig autoBatchConfig = ReferencesBatcher.AutoBatchConfig.defaultConfig()
          .batchSize(2)
          .callback(callback)
          .build();

        try {
          asyncClient.batch().referencesAutoBatcher(batchRetriesConfig, autoBatchConfig)
            .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
            .run()
            .get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      };

      BatchReferencesMockServerTestSuite.testNotCreateAutoBatchReferencesDueToConnectionIssue(supplierReferencesBatcher,
        execMin, execMax);
    }
  }

  public static Object[][] provideForNotCreateBatchReferencesDueToConnectionIssue() {
    return new Object[][]{
      new Object[]{
        // final response should be available immediately
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxConnectionRetries(0)
          .build(),
        0, 100
      },
      new Object[]{
        // final response should be available after 1 retry (200 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxConnectionRetries(1)
          .build(),
        200, 300
      },
      new Object[]{
        // final response should be available after 2 retries (200 + 400 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxConnectionRetries(2)
          .build(),
        600, 700
      },
      new Object[]{
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

    mockServerClient.when(
      request().withMethod("POST").withPath("/v1/batch/references")
    ).respond(
      response().withDelay(Delay.seconds(2)).withStatusCode(200)
    );

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BatchReferenceResponse[]>> supplierReferencesBatcher = () -> {
        try {
          return asyncClient.batch().referencesBatcher(batchRetriesConfig)
            .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
            .run()
            .get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      };
      Consumer<Integer> assertBatchCallsTimes = count -> mockServerClient.verify(
        request().withMethod("POST").withPath("/v1/batch/references"),
        VerificationTimes.exactly(count)
      );

      BatchReferencesMockServerTestSuite.testNotCreateBatchReferencesDueToTimeoutIssue(supplierReferencesBatcher,
        assertBatchCallsTimes, expectedBatchCalls, "1 SECONDS");
    }
  }

  @Test
  @DataMethod(source = ClientBatchReferencesCreateMockServerTest.class,
    method = "provideForNotCreateBatchReferencesDueToTimeoutIssue")
  public void shouldNotCreateAutoBatchReferencesDueToTimeoutIssue(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                                  int expectedBatchCalls) {
    // given client times out after 1s

    mockServerClient.when(
      request().withMethod("POST").withPath("/v1/batch/references")
    ).respond(
      response().withDelay(Delay.seconds(2)).withStatusCode(200)
    );

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Consumer<Consumer<Result<BatchReferenceResponse[]>>> supplierReferencesBatcher = callback -> {
        ReferencesBatcher.AutoBatchConfig autoBatchConfig = ReferencesBatcher.AutoBatchConfig.defaultConfig()
          .batchSize(2)
          .callback(callback)
          .build();

        try {
          asyncClient.batch().referencesAutoBatcher(batchRetriesConfig, autoBatchConfig)
            .withReferences(refPizzaToSoup, refSoupToPizza, refPizzaToPizza, refSoupToSoup)
            .run()
            .get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      };
      Consumer<Integer> assertBatchCallsTimes = count -> mockServerClient.verify(
        request().withMethod("POST").withPath("/v1/batch/references"),
        VerificationTimes.exactly(count)
      );

      BatchReferencesMockServerTestSuite.testNotCreateAutoBatchReferencesDueToTimeoutIssue(supplierReferencesBatcher,
        assertBatchCallsTimes, expectedBatchCalls, "1 SECONDS");
    }
  }

  public static Object[][] provideForNotCreateBatchReferencesDueToTimeoutIssue() {
    return new Object[][]{
      new Object[]{
        // final response should be available immediately
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(0)
          .build(),
        1
      },
      new Object[]{
        // final response should be available after 1 retry (200 ms)
        ReferencesBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(1)
          .build(),
        2
      },
      new Object[]{
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
