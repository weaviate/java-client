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
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.integration.tests.batch.BatchObjectsMockServerTestSuite;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.verify.VerificationTimes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
  public void shouldHandleMultipleRequests() {
    Serializer serializer = new Serializer();
    String[] objectStrings = new String[]{
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1),
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_2),
    };

    Map<String, String> idToObjStr = new HashMap<>();

    int count = 50;
    for (int i = 0; i < count; i++) {
      String id = UUID.randomUUID().toString();
      String objectString = objectStrings[i % objectStrings.length];

      idToObjStr.put(id, objectString);

      mockServerClient.when(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", id))
      ).respond(
        response().withDelay(TimeUnit.MILLISECONDS, 500).withBody(objectString)
      );
    }

    try (WeaviateAsyncClient asyncClient = client.async()) {
      List<Future<Result<List<WeaviateObject>>>> futures = idToObjStr.keySet().stream()
        .map(id -> {
            System.out.printf("[%s] future creating\n", id);

            Future<Result<List<WeaviateObject>>> future = asyncClient.data().objectsGetter()
              .withClassName("Pizza")
              .withID(id)
              .run(new FutureCallback<Result<List<WeaviateObject>>>() {
                @Override
                public void completed(Result<List<WeaviateObject>> listResult) {
                  System.out.printf("[%s] future completed\n", id);
                }

                @Override
                public void failed(Exception e) {
                  System.out.printf("[%s] future failed\n", id);
                }

                @Override
                public void cancelled() {
                  System.out.printf("[%s] future cancelled\n", id);
                }
              });
            System.out.printf("[%s] future created\n", id);

            return future;
          }
        ).collect(Collectors.toList());

      System.out.println("before sleep");
      Thread.sleep(2000);
      System.out.println("after sleep");

      for (Future<Result<List<WeaviateObject>>> future : futures) {
        Result<List<WeaviateObject>> result = future.get();
        assertThat(result).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).isNotNull();
        System.out.println(result.getResult());
      }

    } catch (InterruptedException | ExecutionException e) {
      System.out.println("runtime exception");
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldHandleMultipleRequestsCompletableFuture() {
    Serializer serializer = new Serializer();
    String[] objectStrings = new String[]{
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1),
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_2),
    };

    Map<String, String> idToObjStr = new HashMap<>();

    int count = 50;
    for (int i = 0; i < count; i++) {
      String id = UUID.randomUUID().toString();
      String objectString = objectStrings[i % objectStrings.length];

      idToObjStr.put(id, objectString);

      mockServerClient.when(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", id))
      ).respond(
        response().withDelay(TimeUnit.MILLISECONDS, 500).withBody(objectString)
      );
    }

    try (WeaviateAsyncClient asyncClient = client.async()) {
      List<Future<Result<List<WeaviateObject>>>> futures = idToObjStr.keySet().stream()
        .map(id -> {
            System.out.printf("[%s] future creating\n", id);
            CompletableFuture<Result<List<WeaviateObject>>> future = new CompletableFuture<>();
            asyncClient.data().objectsGetter()
              .withClassName("Pizza")
              .withID(id)
              .run(new FutureCallback<Result<List<WeaviateObject>>>() {
                @Override
                public void completed(Result<List<WeaviateObject>> listResult) {
                  System.out.printf("[%s] future completed\n", id);
                  future.complete(listResult);
                }

                @Override
                public void failed(Exception e) {
                  System.out.printf("[%s] future failed\n    -> exception %s\n", id, e);
                  future.completeExceptionally(e);
                }

                @Override
                public void cancelled() {
                  System.out.printf("[%s] future cancelled\n", id);
                }
              });
            System.out.printf("[%s] future created\n", id);

            return future;
          }
        ).collect(Collectors.toList());

      System.out.println("before sleep");
      Thread.sleep(2000);
      System.out.println("after sleep");

      for (Future<Result<List<WeaviateObject>>> future : futures) {
        Result<List<WeaviateObject>> result = future.get();
        assertThat(result).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).isNotNull();
        System.out.println(result.getResult());
      }

    } catch (InterruptedException | ExecutionException e) {
      System.out.println("runtime exception");
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldHandleMultipleRequestsCompletableFutureAllOf() throws InterruptedException {
    Serializer serializer = new Serializer();
    String[] objectStrings = new String[]{
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1),
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_2),
    };

    Map<String, String> idToObjStr = new HashMap<>();

    int count = 50;
    for (int i = 0; i < count; i++) {
      String id = UUID.randomUUID().toString();
      String objectString = objectStrings[i % objectStrings.length];

      idToObjStr.put(id, objectString);

      mockServerClient.when(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", id))
      ).respond(
        response().withDelay(TimeUnit.MILLISECONDS, 500).withBody(objectString)
      );
    }

    try (WeaviateAsyncClient asyncClient = client.async()) {
      List<CompletableFuture<Result<List<WeaviateObject>>>> futures = idToObjStr.keySet().stream()
        .map(id -> {
            System.out.printf("[%s] future creating\n", id);
            CompletableFuture<Result<List<WeaviateObject>>> future = new CompletableFuture<>();
            asyncClient.data().objectsGetter()
              .withClassName("Pizza")
              .withID(id)
              .run(new FutureCallback<Result<List<WeaviateObject>>>() {
                @Override
                public void completed(Result<List<WeaviateObject>> listResult) {
                  System.out.printf("[%s] future completed\n", id);
                  future.complete(listResult);
                }

                @Override
                public void failed(Exception e) {
                  System.out.printf("[%s] future failed\n    -> exception %s\n", id, e);
                  future.completeExceptionally(e);
                }

                @Override
                public void cancelled() {
                  System.out.printf("[%s] future cancelled\n", id);
                }
              });
            System.out.printf("[%s] future created\n", id);

            return future;
          }
        ).collect(Collectors.toList());

      CompletableFuture<Result<List<WeaviateObject>>>[] completableFutures = futures.toArray(new CompletableFuture[0]);
      CompletableFuture.allOf(completableFutures).join();

      try {
        int idx = 0;
        for (Future<Result<List<WeaviateObject>>> future : futures) {
          Result<List<WeaviateObject>> result = future.get();
          assertThat(result).isNotNull()
            .returns(false, Result::hasErrors)
            .extracting(Result::getResult).isNotNull();
          System.out.printf("[%02d] %s\n", ++idx, result.getResult());
        }
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  public void shouldHandleMultipleRequestsCompletableFutureAllOf2() throws InterruptedException {
    Serializer serializer = new Serializer();
    String[] objectStrings = new String[]{
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1),
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_2),
    };

    Map<String, String> idToObjStr = new HashMap<>();

    int count = 50;
    for (int i = 0; i < count; i++) {
      String id = UUID.randomUUID().toString();
      String objectString = objectStrings[i % objectStrings.length];

      idToObjStr.put(id, objectString);

      mockServerClient.when(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", id))
      ).respond(
        response().withDelay(TimeUnit.MILLISECONDS, 500).withBody(objectString)
      );
    }

    try (WeaviateAsyncClient asyncClient = client.async()) {
      List<CompletableFuture<Result<List<WeaviateObject>>>> futures = idToObjStr.keySet().stream()
        .map(id -> {
            System.out.printf("[%s] future creating\n", id);
            CompletableFuture<Result<List<WeaviateObject>>> future = new CompletableFuture<>();
            asyncClient.data().objectsGetter()
              .withClassName("Pizza")
              .withID(id)
              .run(new FutureCallback<Result<List<WeaviateObject>>>() {
                @Override
                public void completed(Result<List<WeaviateObject>> listResult) {
                  System.out.printf("[%s] future completed\n", id);
                  future.complete(listResult);
                }

                @Override
                public void failed(Exception e) {
                  System.out.printf("[%s] future failed\n    -> exception %s\n", id, e);
                  future.completeExceptionally(e);
                }

                @Override
                public void cancelled() {
                  System.out.printf("[%s] future cancelled\n", id);
                }
              });
            System.out.printf("[%s] future created\n", id);

            return future;
          }
        ).collect(Collectors.toList());

      CompletableFuture<Result<List<WeaviateObject>>>[] completableFutures = futures.toArray(new CompletableFuture[0]);
      CompletableFuture.allOf(completableFutures).whenComplete((v, t) -> {
        try {
          for (Future<Result<List<WeaviateObject>>> future : futures) {
            Result<List<WeaviateObject>> result = future.get();
            assertThat(result).isNotNull()
              .returns(false, Result::hasErrors)
              .extracting(Result::getResult).isNotNull();
            System.out.println(result.getResult());
          }
        } catch (ExecutionException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).join();
    }
  }


  @Test
  public void shouldHandleMultipleRequestsCompletableFutureNested() {
    Serializer serializer = new Serializer();
    String[] objectStrings = new String[]{
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1),
      serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_2),
    };

    int count = 50;
    Map<String, String> idToObjStr = new HashMap<>();
    String[] ids = new String[count];

    for (int i = 0; i < count; i++) {
      String id = UUID.randomUUID().toString();
      String objectString = objectStrings[i % objectStrings.length];

      idToObjStr.put(id, objectString);
      ids[i] = id;

      mockServerClient.when(
        request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", id))
      ).respond(
        response().withDelay(TimeUnit.MILLISECONDS, 500).withBody(objectString)
      );
    }

    String threadName = Thread.currentThread().getName();
    System.out.printf("test starting (%s)\n", threadName);


    try (WeaviateAsyncClient asyncClient = client.async()) {
      ArrayList<WeaviateObject> accumulator = new ArrayList<>(ids.length);
      getByIdRecursively(asyncClient, accumulator, ids, 0).join();

      System.out.printf("accumulator %s\n", accumulator);
    }
  }

  CompletableFuture<Result<List<WeaviateObject>>> getByIdRecursively(WeaviateAsyncClient asyncClient,
                                                                     List<WeaviateObject> accumulator,
                                                                     String[] ids, int counter) {
    return getById(asyncClient, ids[counter], "main", counter).handleAsync((r, t) -> {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s] future handling (%s)\n", ids[counter], threadName);

        if (!r.hasErrors()) {
          accumulator.addAll(r.getResult());
        }

        List<String> nestIds = new ArrayList<>();
        nestIds.add(ids[(counter + 1) % ids.length]);
        nestIds.add(ids[(counter + 2) % ids.length]);
        nestIds.add(ids[(counter + 3) % ids.length]);
        nestIds.add(ids[(counter + 4) % ids.length]);

        List<CompletableFuture<Result<List<WeaviateObject>>>> futures = nestIds.stream()
          .map(id -> getById(asyncClient, id, "nested", counter))
          .collect(Collectors.toList());
        CompletableFuture<Result<List<WeaviateObject>>>[] completableFutures = futures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(completableFutures).join();


        try {
          for (CompletableFuture<Result<List<WeaviateObject>>> f : futures) {
            f.get();
          }
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }

//        if (counter + 1 < ids.length) {
//          return getByIdRecursively(asyncClient, accumulator, ids, counter + 1);
//        }

        Result<List<WeaviateObject>> result = new Result<>(200, accumulator, null);
        return CompletableFuture.completedFuture(result);
      })
      .thenCompose(f -> f);
  }

  CompletableFuture<Result<List<WeaviateObject>>> getById(WeaviateAsyncClient asyncClient, String id, String comment, int counter) {
    String threadName = Thread.currentThread().getName();
    System.out.printf("[%s] future creating (%s %d %s)\n", id, comment, counter, threadName);

    CompletableFuture<Result<List<WeaviateObject>>> future = new CompletableFuture<>();
    asyncClient.data().objectsGetter()
      .withClassName("Pizza")
      .withID(id)
      .run(new FutureCallback<Result<List<WeaviateObject>>>() {
        @Override
        public void completed(Result<List<WeaviateObject>> listResult) {
          String threadName = Thread.currentThread().getName();
          System.out.printf("[%s] future completed (%s %d %s)\n", id, comment, counter, threadName);
          future.complete(listResult);
        }

        @Override
        public void failed(Exception e) {
          String threadName = Thread.currentThread().getName();
          System.out.printf("[%s] future failed (%s %d %s)\n    -> exception %s\n", id, comment, counter, threadName, e);
          future.completeExceptionally(e);
        }

        @Override
        public void cancelled() {
          String threadName = Thread.currentThread().getName();
          System.out.printf("[%s] future cancelled (%s %d %s)\n", id, comment, counter, threadName);
        }
      });
    System.out.printf("[%s] future created (%s %d %s)\n", id, comment, counter, threadName);

    return future;
  }


  @Test
  @DataMethod(source = ClientBatchCreateMockServerTest.class, method = "provideForNotCreateBatchDueToTimeoutIssue")
  public void shouldNotCreateBatchDueToTimeoutIssue(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                                    int expectedBatchCallsCount) {
    // given client times out after 1s
    System.out.println("shouldNotCreateBatchDueToTimeoutIssue starting");
    Serializer serializer = new Serializer();
    String pizza1Str = serializer.toJsonString(BatchObjectsMockServerTestSuite.PIZZA_1);
    String soup1Str = serializer.toJsonString(BatchObjectsMockServerTestSuite.SOUP_1);

    System.out.println("shouldNotCreateBatchDueToTimeoutIssue serialized");

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
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Pizza", BatchObjectsMockServerTestSuite.PIZZA_2_ID))
    ).respond(
      response().withStatusCode(404)
    );
    mockServerClient.when(
      request().withMethod("GET").withPath(String.format("/v1/objects/%s/%s", "Soup", BatchObjectsMockServerTestSuite.SOUP_2_ID))
    ).respond(
      response().withStatusCode(404)
    );

    System.out.println("shouldNotCreateBatchDueToTimeoutIssue mocked");

    try (WeaviateAsyncClient asyncClient = client.async()) {
      System.out.println("shouldNotCreateBatchDueToTimeoutIssue async created");

      Supplier<Result<ObjectGetResponse[]>> supplierObjectsBatcher = () -> {
        try {
          Future<Result<ObjectGetResponse[]>> run = asyncClient.batch().objectsBatcher(batchRetriesConfig)
            .withObjects(BatchObjectsMockServerTestSuite.PIZZA_1, BatchObjectsMockServerTestSuite.PIZZA_2,
              BatchObjectsMockServerTestSuite.SOUP_1, BatchObjectsMockServerTestSuite.SOUP_2)
            .run();
          System.out.println("shouldNotCreateBatchDueToTimeoutIssue async run");
          return run
            .get();
        } catch (InterruptedException | ExecutionException e) {
          System.out.println("runtime exception");
          throw new RuntimeException(e);
        }
      };
      System.out.println("shouldNotCreateBatchDueToTimeoutIssue supplier");

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

      System.out.println("shouldNotCreateBatchDueToTimeoutIssue consumers");


      BatchObjectsMockServerTestSuite.testNotCreateBatchDueToTimeoutIssue(supplierObjectsBatcher,
        assertPostObjectsCallsCount, assertGetPizza1CallsCount, assertGetPizza2CallsCount,
        assertGetSoup1CallsCount, assertGetSoup2CallsCount, expectedBatchCallsCount, "1 SECONDS");
    }
    System.out.println("shouldNotCreateBatchDueToTimeoutIssue finished");
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
      new Object[]{
        // final response should be available immediately
        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
          .retriesIntervalMs(200)
          .maxTimeoutRetries(0)
          .build(),
        1
      },
//      new Object[]{
//        // final response should be available after 1 retry (200 ms)
//        ObjectsBatcher.BatchRetriesConfig.defaultConfig()
//          .retriesIntervalMs(200)
//          .maxTimeoutRetries(1)
//          .build(),
//        2
//      },
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
