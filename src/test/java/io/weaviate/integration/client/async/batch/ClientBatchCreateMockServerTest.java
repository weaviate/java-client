package io.weaviate.integration.client.async.batch;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.Serializer;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.integration.tests.batch.BatchObjectsMockServerTestSuite;
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
public class ClientBatchCreateMockServerTest {

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

//  @Test
//  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToConnectionIssue")
//  public void shouldNotCreateBatchDueToConnectionIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
//                                                       long expectedExecMinMillis, long expectedExecMaxMillis) {
//    // stop server to simulate connection issues
//    mockServer.stop();
//
//    try (WeaviateAsyncClient asyncClient = client.async()) {
//      Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher = () -> {
//        try {
//          return asyncClient.batch().objectsBatcher(batchRetriesConfig)
//            .withObjects(BatchObjectsMockServerTestSuite.PIZZA_1, BatchObjectsMockServerTestSuite.PIZZA_2,
//              BatchObjectsMockServerTestSuite.SOUP_1, BatchObjectsMockServerTestSuite.SOUP_2)
//            .run()
//            .get();
//        } catch (InterruptedException | ExecutionException e) {
//          throw new RuntimeException(e);
//        }
//      };
//
//      BatchObjectsMockServerTestSuite.testNotCreateBatchDueToConnectionIssue(supplierObjectsBatcher,
//        expectedExecMinMillis, expectedExecMaxMillis);
//    }
//  }
//
//  @Test
//  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToConnectionIssue")
//  public void shouldNotCreateAutoBatchDueToConnectionIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
//                                                           long expectedExecMinMillis, long expectedExecMaxMillis) {
//    // stop server to simulate connection issues
//    mockServer.stop();
//
//    try (WeaviateAsyncClient asyncClient = client.async()) {
//      Consumer<Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcher = callback -> {
//        ObjectsBatcher.AutoBatchConfig autoBatchConfig = ObjectsBatcher.AutoBatchConfig.defaultConfig()
//          .batchSize(2)
//          .callback(callback)
//          .build();
//
//        try {
//          asyncClient.batch().objectsAutoBatcher(batchRetriesConfig, autoBatchConfig)
//            .withObjects(BatchObjectsMockServerTestSuite.PIZZA_1, BatchObjectsMockServerTestSuite.PIZZA_2,
//              BatchObjectsMockServerTestSuite.SOUP_1, BatchObjectsMockServerTestSuite.SOUP_2)
//            .run()
//            .get();
//        } catch (InterruptedException | ExecutionException e) {
//          throw new RuntimeException(e);
//        }
//      };
//
//      BatchObjectsMockServerTestSuite.testNotCreateAutoBatchDueToConnectionIssue(supplierObjectsBatcher,
//        expectedExecMinMillis, expectedExecMaxMillis);
//    }
//  }
//
//  public static Object[][] provideForNotCreateBatchDueToConnectionIssue() {
//    return new Object[][]{
//      new Object[]{
//        // final response should be available immediately
//        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
//          .retriesIntervalMs(400)
//          .maxConnectionRetries(0)
//          .build(),
//        0, 350
//      },
//      new Object[]{
//        // final response should be available after 1 retry (400 ms)
//        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
//          .retriesIntervalMs(400)
//          .maxConnectionRetries(1)
//          .build(),
//        400, 750
//      },
//      new Object[]{
//        // final response should be available after 2 retries (400 + 800 ms)
//        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
//          .retriesIntervalMs(400)
//          .maxConnectionRetries(2)
//          .build(),
//        1200, 1550
//      },
//      new Object[]{
//        // final response should be available after 1 retry (400 + 800 + 1200 ms)
//        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
//          .retriesIntervalMs(400)
//          .maxConnectionRetries(3)
//          .build(),
//        2400, 2750
//      },
//    };
//  }

  @Test
  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToTimeoutIssue")
  public void shouldNotCreateBatchDueToTimeoutIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                                    int expectedBatchCallsCount) {
    // given client times out after 1s

    Serializer serializer = new Serializer();
    String pizza1Str = serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1);
    String soup1Str = serializer.toJsonString(BatchObjectsMockServerTestSuite.SOUP_1);

    // batch request should end up with timeout exception, but Pizza1 and Soup1 should be "added" and available by get
    mockServerClient.when(
      request().withMethod("POST").withPath("/v1/batch/objects")
    ).respond(
      response().withDelay(Delay.seconds(2)).withStatusCode(200)
    );
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", BatchObjectsMockServerTestSuite.PIZZA_1_ID))
    ).respond(
      response().withBody(pizza1Str)
    );
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", BatchObjectsMockServerTestSuite.SOUP_1_ID))
    ).respond(
      response().withBody(soup1Str)
    );

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher = () -> {
        try {
          return asyncClient.batch().objectsBatcher(batchRetriesConfig)
            .withObjects(BatchObjectsMockServerTestSuite.PIZZA_1, BatchObjectsMockServerTestSuite.PIZZA_2,
              BatchObjectsMockServerTestSuite.SOUP_1, BatchObjectsMockServerTestSuite.SOUP_2)
            .run()
            .get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      };
      Consumer<Integer> assertPostObjectsCallsCount = count -> mockServerClient.verify(
        request().withMethod("POST").withPath("/v1/batch/objects"),
        VerificationTimes.exactly(count)
      );
      Consumer<Integer> assertGetPizza1CallsCount = count -> mockServerClient.verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", BatchObjectsMockServerTestSuite.PIZZA_1_ID)),
        VerificationTimes.exactly(count)
      );
      Consumer<Integer> assertGetPizza2CallsCount = count -> mockServerClient.verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", BatchObjectsMockServerTestSuite.PIZZA_2_ID)),
        VerificationTimes.exactly(count)
      );
      Consumer<Integer> assertGetSoup1CallsCount = count -> mockServerClient.verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", BatchObjectsMockServerTestSuite.SOUP_1_ID)),
        VerificationTimes.exactly(count)
      );
      Consumer<Integer> assertGetSoup2CallsCount = count -> mockServerClient.verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", BatchObjectsMockServerTestSuite.SOUP_2_ID)),
        VerificationTimes.exactly(count)
      );

      BatchObjectsMockServerTestSuite.testNotCreateBatchDueToTimeoutIssue(supplierObjectsBatcher,
        assertPostObjectsCallsCount, assertGetPizza1CallsCount, assertGetPizza2CallsCount,
        assertGetSoup1CallsCount, assertGetSoup2CallsCount, expectedBatchCallsCount, "1 SECONDS");
    }
  }

//  @Test
//  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToTimeoutIssue")
//  public void shouldNotCreateAutoBatchDueToTimeoutIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
//                                                        int expectedBatchCallsCount) {
//    // given client times out after 1s
//
//    Serializer serializer = new Serializer();
//    String pizza1Str = serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1);
//    String soup1Str = serializer.toJsonString(BatchObjectsMockServerTestSuite.SOUP_1);
//
//    // batch request should end up with timeout exception, but Pizza1 and Soup1 should be "added" and available by get
//    mockServerClient.when(
//      request().withMethod("POST").withPath("/v1/batch/objects")
//    ).respond(
//      response().withDelay(Delay.seconds(2)).withStatusCode(200)
//    );
//    mockServerClient.when(
//      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", BatchObjectsMockServerTestSuite.PIZZA_1_ID))
//    ).respond(
//      response().withBody(pizza1Str)
//    );
//    mockServerClient.when(
//      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", BatchObjectsMockServerTestSuite.SOUP_1_ID))
//    ).respond(
//      response().withBody(soup1Str)
//    );
//
//    try (WeaviateAsyncClient asyncClient = client.async()) {
//      Consumer<Consumer<Result<ObjectGetResponse[]>>> supplierObjectsBatcher = callback -> {
//        ObjectsBatcher.AutoBatchConfig autoBatchConfig = ObjectsBatcher.AutoBatchConfig.defaultConfig()
//          .batchSize(2)
//          .callback(callback)
//          .build();
//
//        try {
//          asyncClient.batch().objectsAutoBatcher(batchRetriesConfig, autoBatchConfig)
//            .withObjects(BatchObjectsMockServerTestSuite.PIZZA_1, BatchObjectsMockServerTestSuite.PIZZA_2,
//              BatchObjectsMockServerTestSuite.SOUP_1, BatchObjectsMockServerTestSuite.SOUP_2)
//            .run()
//            .get();
//        } catch (InterruptedException | ExecutionException e) {
//          throw new RuntimeException(e);
//        }
//      };
//
//      Consumer<Integer> assertPostObjectsCallsCount = count -> mockServerClient.verify(
//        request().withMethod("POST").withPath("/v1/batch/objects"),
//        VerificationTimes.exactly(count)
//      );
//      Consumer<Integer> assertGetPizza1CallsCount = count -> mockServerClient.verify(
//        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", BatchObjectsMockServerTestSuite.PIZZA_1_ID)),
//        VerificationTimes.exactly(count)
//      );
//      Consumer<Integer> assertGetPizza2CallsCount = count -> mockServerClient.verify(
//        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", BatchObjectsMockServerTestSuite.PIZZA_2_ID)),
//        VerificationTimes.exactly(count)
//      );
//      Consumer<Integer> assertGetSoup1CallsCount = count -> mockServerClient.verify(
//        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", BatchObjectsMockServerTestSuite.SOUP_1_ID)),
//        VerificationTimes.exactly(count)
//      );
//      Consumer<Integer> assertGetSoup2CallsCount = count -> mockServerClient.verify(
//        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", BatchObjectsMockServerTestSuite.SOUP_2_ID)),
//        VerificationTimes.exactly(count)
//      );
//
//      BatchObjectsMockServerTestSuite.testNotCreateAutoBatchDueToTimeoutIssue(supplierObjectsBatcher,
//        assertPostObjectsCallsCount, assertGetPizza1CallsCount, assertGetPizza2CallsCount,
//        assertGetSoup1CallsCount, assertGetSoup2CallsCount, expectedBatchCallsCount, "1 SECONDS");
//    }
//  }

  public static Object[][] provideForNotCreateBatchDueToTimeoutIssue() {
    return new Object[][]{
//      new Object[]{
//        // final response should be available immediately
//        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
//          .retriesIntervalMs(200)
//          .maxTimeoutRetries(0)
//          .build(),
//        1
//      },
      new Object[]{
        // final response should be available after 1 retry (200 ms)
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(1)
          .build(),
        2
      },
//      new Object[]{
//        // final response should be available after 2 retries (200 + 400 ms)
//        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
//          .retriesIntervalMs(200)
//          .maxTimeoutRetries(2)
//          .build(),
//        3
//      },
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
