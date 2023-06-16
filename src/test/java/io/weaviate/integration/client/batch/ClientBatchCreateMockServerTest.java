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
import io.weaviate.client.base.Serializer;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(JParamsTestRunner.class)
public class ClientBatchCreateMockServerTest {

  private static final String PIZZA_1_ID = "abefd256-8574-442b-9293-9205193737ee";
  private static final Map<String, Object> PIZZA_1_PROPS = createFoodProperties("Hawaii", "Universally accepted to be the best pizza ever created.");
  private static final String PIZZA_2_ID = "97fa5147-bdad-4d74-9a81-f8babc811b09";
  private static final Map<String, Object> PIZZA_2_PROPS = createFoodProperties("Doener", "A innovation, some say revolution, in the pizza industry.");
  private static final String SOUP_1_ID = "565da3b6-60b3-40e5-ba21-e6bfe5dbba91";
  private static final Map<String, Object> SOUP_1_PROPS = createFoodProperties("ChickenSoup", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
  private static final String SOUP_2_ID = "07473b34-0ab2-4120-882d-303d9e13f7af";
  private static final Map<String, Object> SOUP_2_PROPS = createFoodProperties("Beautiful", "Putting the game of letter soups to a whole new level.");
  
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
  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToConnectionIssue")
  public void shouldNotCreateBatchDueToConnectionIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig, long execMin, long execMax) {
    // stop server to simulate connection issues
    mockServer.stop();

    WeaviateObject[] objects = {
      WeaviateObject.builder().className("Pizza").id(PIZZA_1_ID).properties(PIZZA_1_PROPS).build(),
      WeaviateObject.builder().className("Pizza").id(PIZZA_2_ID).properties(PIZZA_2_PROPS).build(),
      WeaviateObject.builder().className("Soup").id(SOUP_1_ID).properties(SOUP_1_PROPS).build(),
      WeaviateObject.builder().className("Soup").id(SOUP_2_ID).properties(SOUP_2_PROPS).build()
    };

    ZonedDateTime start = ZonedDateTime.now();
    Result<ObjectGetResponse[]> resBatch = client.batch().objectsBatcher(batchRetriesConfig)
      .withObjects(objects)
      .run();
    ZonedDateTime end = ZonedDateTime.now();

    assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(execMin, execMax);
    assertThat(resBatch.getResult()).isNull();
    assertThat(resBatch.hasErrors()).isTrue();

    List<WeaviateErrorMessage> errorMessages = resBatch.getError().getMessages();
    assertThat(errorMessages).hasSize(2);
    assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(ConnectException.class);
    assertThat(errorMessages.get(0).getMessage()).contains("Connection refused");
    assertThat(errorMessages.get(1).getThrowable()).isNull();
    assertThat(errorMessages.get(1).getMessage()).contains(PIZZA_1_ID, PIZZA_2_ID, SOUP_1_ID, SOUP_2_ID);
  }

  @Test
  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToConnectionIssue")
  public void shouldNotCreateAutoBatchDueToConnectionIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                                           long expectedExecMinMillis, long expectedExecMaxMillis) {
    // stop server to simulate connection issues
    mockServer.stop();

    WeaviateObject[] objects = {
      WeaviateObject.builder().className("Pizza").id(PIZZA_1_ID).properties(PIZZA_1_PROPS).build(),
      WeaviateObject.builder().className("Pizza").id(PIZZA_2_ID).properties(PIZZA_2_PROPS).build(),
      WeaviateObject.builder().className("Soup").id(SOUP_1_ID).properties(SOUP_1_PROPS).build(),
      WeaviateObject.builder().className("Soup").id(SOUP_2_ID).properties(SOUP_2_PROPS).build()
    };

    List<Result<ObjectGetResponse[]>> resBatches = Collections.synchronizedList(new ArrayList<>(2));
    ObjectsBatcher.AutoBatchConfig autoBatchConfig = ObjectsBatcher.AutoBatchConfig.defaultConfig()
      .batchSize(2)
      .poolSize(1)
      .callback(resBatches::add)
      .build();

    ZonedDateTime start = ZonedDateTime.now();
    client.batch().objectsAutoBatcher(batchRetriesConfig, autoBatchConfig)
      .withObjects(objects)
      .flush();
    ZonedDateTime end = ZonedDateTime.now();

    assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(expectedExecMinMillis, expectedExecMaxMillis);
    assertThat(resBatches).hasSize(2);

    for (Result<ObjectGetResponse[]> resBatch: resBatches) {
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

  public static Object[][] provideForNotCreateBatchDueToConnectionIssue() {
    return new Object[][] {
      new Object[] {
        // final response should be available immediately
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(400)
          .maxConnectionRetries(0)
          .build(),
        0, 350
      },
      new Object[] {
        // final response should be available after 1 retry (400 ms)
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(400)
          .maxConnectionRetries(1)
          .build(),
        400, 750
      },
      new Object[] {
        // final response should be available after 2 retries (400 + 800 ms)
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(400)
          .maxConnectionRetries(2)
          .build(),
        1200, 1550
      },
      new Object[] {
        // final response should be available after 1 retry (400 + 800 + 1200 ms)
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(400)
          .maxConnectionRetries(3)
          .build(),
        2400, 2750
      },
    };
  }

  @Test
  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToTimeoutIssue")
  public void shouldNotCreateBatchDueToTimeoutIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                                    int expectedBatchCalls) {
    // given client times out after 1s

    WeaviateObject pizza1 = WeaviateObject.builder().className("Pizza").id(PIZZA_1_ID).properties(PIZZA_1_PROPS).build();
    WeaviateObject pizza2 = WeaviateObject.builder().className("Pizza").id(PIZZA_2_ID).properties(PIZZA_2_PROPS).build();
    WeaviateObject soup1 = WeaviateObject.builder().className("Soup").id(SOUP_1_ID).properties(SOUP_1_PROPS).build();
    WeaviateObject soup2 = WeaviateObject.builder().className("Soup").id(SOUP_2_ID).properties(SOUP_2_PROPS).build();
    WeaviateObject[] objects = {pizza1, pizza2, soup1, soup2};

    Serializer serializer = new Serializer();
    String pizza1Str = serializer.toJsonString(pizza1);
    String soup1Str = serializer.toJsonString(soup1);

    // batch request should end up with timeout exception, but Pizza1 and Soup1 should be "added" and available by get
    mockServerClient.when(
      request().withMethod("POST").withPath("/v1/batch/objects")
    ).respond(
      response().withDelay(Delay.seconds(2)).withStatusCode(200)
    );
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", PIZZA_1_ID))
    ).respond(
      response().withBody(pizza1Str)
    );
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", SOUP_1_ID))
    ).respond(
      response().withBody(soup1Str)
    );

    Result<ObjectGetResponse[]> resBatch = client.batch().objectsBatcher(batchRetriesConfig)
      .withObjects(objects)
      .run();

    mockServerClient
      .verify(
        request().withMethod("POST").withPath("/v1/batch/objects"),
        VerificationTimes.exactly(expectedBatchCalls)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", PIZZA_2_ID)),
        VerificationTimes.exactly(expectedBatchCalls)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", SOUP_2_ID)),
        VerificationTimes.exactly(expectedBatchCalls)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", PIZZA_1_ID)),
        VerificationTimes.exactly(1)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", SOUP_1_ID)),
        VerificationTimes.exactly(1)
      );

    assertThat(resBatch.getResult()).hasSize(2);
    assertThat(resBatch.hasErrors()).isTrue();

    List<WeaviateErrorMessage> errorMessages = resBatch.getError().getMessages();
    assertThat(errorMessages).hasSize(2);
    assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
    assertThat(errorMessages.get(0).getMessage()).contains("Read timed out");
    assertThat(errorMessages.get(1).getThrowable()).isNull();
    assertThat(errorMessages.get(1).getMessage()).contains(PIZZA_2_ID, SOUP_2_ID).doesNotContain(PIZZA_1_ID, SOUP_1_ID);

    assertThat(resBatch.getResult()[0].getId()).isEqualTo(PIZZA_1_ID);
    assertThat(resBatch.getResult()[1].getId()).isEqualTo(SOUP_1_ID);
  }


  @Test
  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToTimeoutIssue")
  public void shouldNotCreateAutoBatchDueToTimeoutIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                                        int expectedBatchCalls) {
    // given client times out after 1s

    WeaviateObject pizza1 = WeaviateObject.builder().className("Pizza").id(PIZZA_1_ID).properties(PIZZA_1_PROPS).build();
    WeaviateObject pizza2 = WeaviateObject.builder().className("Pizza").id(PIZZA_2_ID).properties(PIZZA_2_PROPS).build();
    WeaviateObject soup1 = WeaviateObject.builder().className("Soup").id(SOUP_1_ID).properties(SOUP_1_PROPS).build();
    WeaviateObject soup2 = WeaviateObject.builder().className("Soup").id(SOUP_2_ID).properties(SOUP_2_PROPS).build();
    WeaviateObject[] objects = {pizza1, pizza2, soup1, soup2};

    Serializer serializer = new Serializer();
    String pizza1Str = serializer.toJsonString(pizza1);
    String soup1Str = serializer.toJsonString(soup1);

    // batch request should end up with timeout exception, but Pizza1 and Soup1 should be "added" and available by get
    mockServerClient.when(
      request().withMethod("POST").withPath("/v1/batch/objects")
    ).respond(
      response().withDelay(Delay.seconds(2)).withStatusCode(200)
    );
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", PIZZA_1_ID))
    ).respond(
      response().withBody(pizza1Str)
    );
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", SOUP_1_ID))
    ).respond(
      response().withBody(soup1Str)
    );

    List<Result<ObjectGetResponse[]>> resBatches = Collections.synchronizedList(new ArrayList<>(2));
    ObjectsBatcher.AutoBatchConfig autoBatchConfig = ObjectsBatcher.AutoBatchConfig.defaultConfig()
      .batchSize(2)
      .poolSize(2)
      .callback(resBatches::add)
      .build();

    client.batch().objectsAutoBatcher(batchRetriesConfig, autoBatchConfig)
      .withObjects(objects)
      .flush();

    mockServerClient
      .verify(
        request().withMethod("POST").withPath("/v1/batch/objects"),
        VerificationTimes.exactly(expectedBatchCalls * 2)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", PIZZA_2_ID)),
        VerificationTimes.exactly(expectedBatchCalls)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", SOUP_2_ID)),
        VerificationTimes.exactly(expectedBatchCalls)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", PIZZA_1_ID)),
        VerificationTimes.exactly(1)
      )
      .verify(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", SOUP_1_ID)),
        VerificationTimes.exactly(1)
      );

    assertThat(resBatches).hasSize(2);

    for (Result<ObjectGetResponse[]> resBatch: resBatches) {
      assertThat(resBatch.getResult()).hasSize(1);
      assertThat(resBatch.hasErrors()).isTrue();

      List<WeaviateErrorMessage> errorMessages = resBatch.getError().getMessages();
      assertThat(errorMessages).hasSize(2);
      assertThat(errorMessages.get(0).getThrowable()).isInstanceOf(SocketTimeoutException.class);
      assertThat(errorMessages.get(0).getMessage()).contains("Read timed out");
      assertThat(errorMessages.get(1).getThrowable()).isNull();

      String failedIdsMessage = errorMessages.get(1).getMessage();
      if (failedIdsMessage.contains(PIZZA_2_ID)) {
        assertThat(failedIdsMessage).contains(PIZZA_2_ID).doesNotContain(PIZZA_1_ID, SOUP_1_ID, SOUP_2_ID);
        assertThat(resBatch.getResult()[0].getId()).isEqualTo(PIZZA_1_ID);
      } else {
        assertThat(failedIdsMessage).contains(SOUP_2_ID).doesNotContain(PIZZA_1_ID, PIZZA_2_ID, SOUP_1_ID);
        assertThat(resBatch.getResult()[0].getId()).isEqualTo(SOUP_1_ID);
      }
    }
  }

  public static Object[][] provideForNotCreateBatchDueToTimeoutIssue() {
    return new Object[][] {
      new Object[] {
        // final response should be available immediately
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(0)
          .build(),
        1
      },
      new Object[] {
        // final response should be available after 1 retry (200 ms)
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(1)
          .build(),
        2
      },
      new Object[] {
        // final response should be available after 2 retries (200 + 400 ms)
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(2)
          .build(),
        3
      },
    };
  }

  private static Map<String, Object> createFoodProperties(String name, String description) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", name);
    props.put("description", description);

    return props;
  }

  private String metaBody() {
    return String.format("{\n" +
      "  \"hostname\": \"http://[::]:%s\",\n" +
      "  \"modules\": {},\n" +
      "  \"version\": \"%s\"\n" +
      "}", MOCK_SERVER_PORT, "1.17.999-mock-server-version");
  }
}
