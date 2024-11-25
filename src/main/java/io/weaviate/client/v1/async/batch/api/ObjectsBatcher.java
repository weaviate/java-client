package io.weaviate.client.v1.async.batch.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.grpc.AsyncGrpcClient;
import io.weaviate.client.base.util.Assert;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch;
import io.weaviate.client.v1.async.data.Data;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.batch.grpc.BatchObjectConverter;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponseStatus;
import io.weaviate.client.v1.batch.model.ObjectsBatchRequestBody;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.batch.util.ObjectsPath;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectsBatcher extends AsyncBaseClient<ObjectGetResponse[]>
  implements AsyncClientResult<ObjectGetResponse[]> {

  private final Data data;
  private final ObjectsPath objectsPath;
  private final AccessTokenProvider tokenProvider;
  private final GrpcVersionSupport grpcVersionSupport;

  private final ObjectsBatcher.BatchRetriesConfig batchRetriesConfig;
  private final ObjectsBatcher.AutoBatchConfig autoBatchConfig;
  private final Config config;
  private final boolean autoRunEnabled;
  private final List<CompletableFuture<Result<ObjectGetResponse[]>>> futures;

  private final List<WeaviateObject> objects;
  private String consistencyLevel;


  private ObjectsBatcher(CloseableHttpAsyncClient client, Config config, Data data, ObjectsPath objectsPath,
                         AccessTokenProvider tokenProvider, GrpcVersionSupport grpcVersionSupport,
                         ObjectsBatcher.BatchRetriesConfig batchRetriesConfig, ObjectsBatcher.AutoBatchConfig autoBatchConfig) {
    super(client, config);
    this.config = config;
    this.tokenProvider = tokenProvider;
    this.data = data;
    this.objectsPath = objectsPath;
    this.grpcVersionSupport = grpcVersionSupport;
    this.batchRetriesConfig = batchRetriesConfig;
    this.objects = Collections.synchronizedList(new ArrayList<>());
    this.futures = Collections.synchronizedList(new ArrayList<>());

    if (autoBatchConfig != null) {
      this.autoRunEnabled = true;
      this.autoBatchConfig = autoBatchConfig;
    } else {
      this.autoRunEnabled = false;
      this.autoBatchConfig = null;
    }
  }

  public static ObjectsBatcher create(CloseableHttpAsyncClient client, Config config, Data data, ObjectsPath objectsPath,
                                      AccessTokenProvider tokenProvider, GrpcVersionSupport grpcVersionSupport,
                                      ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    return new ObjectsBatcher(client, config, data, objectsPath, tokenProvider, grpcVersionSupport, batchRetriesConfig, null);
  }

  public static ObjectsBatcher createAuto(CloseableHttpAsyncClient client, Config config, Data data, ObjectsPath objectsPath,
                                          AccessTokenProvider tokenProvider, GrpcVersionSupport grpcVersionSupport,
                                          ObjectsBatcher.BatchRetriesConfig batchRetriesConfig, ObjectsBatcher.AutoBatchConfig autoBatchConfig) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    Assert.requiredNotNull(autoBatchConfig, "autoBatchConfig");
    return new ObjectsBatcher(client, config, data, objectsPath, tokenProvider, grpcVersionSupport, batchRetriesConfig, autoBatchConfig);
  }


  public ObjectsBatcher withObject(WeaviateObject object) {
    return withObjects(object);
  }

  public ObjectsBatcher withObjects(WeaviateObject... objects) {
    addMissingIds(objects);
    this.objects.addAll(Arrays.asList(objects));
    autoRun();
    return this;
  }

  public ObjectsBatcher withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  private void addMissingIds(WeaviateObject[] objects) {
    Arrays.stream(objects)
      .filter(o -> o.getId() == null)
      .forEach(o -> o.setId(UUID.randomUUID().toString()));
  }

  private List<WeaviateObject> extractBatch(int batchSize) {
    List<WeaviateObject> batch = new ArrayList<>(batchSize);
    List<WeaviateObject> sublist = objects.subList(0, batchSize);

    batch.addAll(sublist);
    sublist.clear();

    return batch;
  }

  private void autoRun() {
    if (!autoRunEnabled) {
      return;
    }

    while (objects.size() >= autoBatchConfig.batchSize) {
      List<WeaviateObject> batch = extractBatch(autoBatchConfig.batchSize);
      runBatch(batch);
    }
  }

  @Override
  public Future<Result<ObjectGetResponse[]>> run(FutureCallback<Result<ObjectGetResponse[]>> callback) {
    System.out.println("batcher run started");
    CompletableFuture<Result<ObjectGetResponse[]>> future = runAll();
    System.out.println("batcher run add callback");
    if (callback != null) {
      future = future.whenComplete((result, throwable) -> {
        if (throwable != null) {
          callback.failed((Exception) throwable);
        } else {
          callback.completed(result);
        }
      });
    }
    System.out.println("batcher run finished");
    return future;
  }

  private CompletableFuture<Result<ObjectGetResponse[]>> runAll() {
    if (!autoRunEnabled) {
      System.out.println("batcher runAll started");
      if (objects.isEmpty()) {
        System.out.println("batcher runAll objects empty");
        return CompletableFuture.completedFuture(new Result<>(0, new ObjectGetResponse[0], null));
      }

      System.out.println("batcher runAll extract batch");
      List<WeaviateObject> batch = extractBatch(objects.size());
      System.out.println("batcher runAll runBatchRecursively");
      return runBatchRecursively(batch, 0, 0, null);
    }

    if (!objects.isEmpty()) {
      List<WeaviateObject> batch = extractBatch(objects.size());
      runBatch(batch);
    }
    if (futures.isEmpty()) {
      return CompletableFuture.completedFuture(new Result<>(0, new ObjectGetResponse[0], null));
    }

    CompletableFuture<?>[] futuresAsArray = futures.toArray(new CompletableFuture<?>[0]);
    return CompletableFuture.allOf(futuresAsArray).thenApply(v -> {
      List<ObjectGetResponse> allResponses = new ArrayList<>();
      List<WeaviateErrorMessage> allMessages = new ArrayList<>();
      int[] lastErrStatusCode = new int[]{HttpStatus.SC_OK};

      futures.stream().map(resultCompletableFuture -> {
        try {
          return resultCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new CompletionException(e);
        }
      }).forEach(result -> {
        Optional.ofNullable(result)
          .map(Result::getResult)
          .map(Arrays::asList)
          .ifPresent(allResponses::addAll);
        Optional.ofNullable(result)
          .filter(Result::hasErrors)
          .map(Result::getError)
          .map(WeaviateError::getMessages)
          .ifPresent(allMessages::addAll);
        Optional.ofNullable(result)
          .filter(Result::hasErrors)
          .map(Result::getError)
          .map(WeaviateError::getStatusCode)
          .ifPresent(sc -> lastErrStatusCode[0] = sc);
      });

      WeaviateErrorResponse errorResponse = allMessages.isEmpty()
        ? null
        : WeaviateErrorResponse.builder().error(allMessages).code(lastErrStatusCode[0]).build();
      return new Result<>(lastErrStatusCode[0], allResponses.toArray(new ObjectGetResponse[0]), errorResponse);
    });
  }

  private void runBatch(List<WeaviateObject> batch) {
    CompletableFuture<Result<ObjectGetResponse[]>> future = runBatchRecursively(batch, 0, 0, null);
    if (autoBatchConfig.callback != null) {
      future = future.whenComplete((result, e) -> autoBatchConfig.callback.accept(result));
    }
    futures.add(future);
  }

  private CompletableFuture<Result<ObjectGetResponse[]>> runBatchRecursively(List<WeaviateObject> batch,
                                                                             int connectionErrorCount, int timeoutErrorCount,
                                                                             List<ObjectGetResponse> combinedSingleResponses) {
    System.out.println("batcher runBatchRecursively started");

    return internalRun(batch).handle((Result<ObjectGetResponse[]> result, Throwable throwable) -> {
        System.out.println("batcher runBatchRecursively handle started");

        int lambdaConnectionErrorCount = connectionErrorCount;
        int lambdaTimeErrorCount = timeoutErrorCount;
        List<ObjectGetResponse> lambdaCombinedSingleResponses = combinedSingleResponses;
        List<WeaviateObject> lambdaBatch = batch;

        if (throwable != null) {
          System.out.println("batcher runBatchRecursively handle throwable != null");

          boolean executeAgain = false;
          int delay = 0;

          if (throwable instanceof ConnectException) {
            System.out.println("batcher runBatchRecursively handle ConnectException");

            if (lambdaConnectionErrorCount++ < batchRetriesConfig.maxConnectionRetries) {
              executeAgain = true;
              delay = lambdaConnectionErrorCount * batchRetriesConfig.retriesIntervalMs;
            }
          } else if (throwable instanceof SocketTimeoutException) {
            System.out.println("batcher runBatchRecursively handle SocketTimeoutException");

            Pair<List<ObjectGetResponse>, List<WeaviateObject>> pair = fetchCreatedAndBuildBatchToReRun(lambdaBatch);
            lambdaCombinedSingleResponses = combineSingleResponses(lambdaCombinedSingleResponses, pair.getLeft());
            lambdaBatch = pair.getRight();
            System.out.println(lambdaCombinedSingleResponses);
            System.out.println(lambdaBatch);
            if (ObjectUtils.isNotEmpty(lambdaBatch) && lambdaTimeErrorCount++ < batchRetriesConfig.maxTimeoutRetries) {
              executeAgain = true;
              delay = lambdaTimeErrorCount * batchRetriesConfig.retriesIntervalMs;
            }
          }
          if (executeAgain) {
            System.out.println("batcher runBatchRecursively handle executeAgain");
            try {
              Thread.sleep(delay);
              return runBatchRecursively(lambdaBatch, lambdaConnectionErrorCount, lambdaTimeErrorCount, lambdaCombinedSingleResponses);
            } catch (InterruptedException e) {
              throw new CompletionException(e);
            }
          }
        } else if (!result.hasErrors()) {
          System.out.println("batcher runBatchRecursively handle !result.hasErrors()");

          lambdaBatch = null;
        }

        System.out.println("batcher runBatchRecursively handle return");
        return CompletableFuture.completedFuture(createFinalResultFromLastResultAndCombinedSingleResponses(result,
          throwable, lambdaCombinedSingleResponses, lambdaBatch));
      })
      .thenCompose(f -> {
        System.out.println("batcher runBatchRecursively compose");
        return f;
      });
  }

  private CompletableFuture<Result<ObjectGetResponse[]>> internalRun(List<WeaviateObject> batch) {
    System.out.println("batcher internalRun started");
    return config.useGRPC() ? internalGrpcRun(batch) : internalHttpRun(batch);
  }

  private CompletableFuture<Result<ObjectGetResponse[]>> internalGrpcRun(List<WeaviateObject> batch) {
    System.out.println("batcher internalGrpcRun started");
    BatchObjectConverter batchObjectConverter = new BatchObjectConverter(grpcVersionSupport);
    List<WeaviateProtoBatch.BatchObject> batchObjects = batch.stream()
      .map(batchObjectConverter::toBatchObject)
      .collect(Collectors.toList());

    WeaviateProtoBatch.BatchObjectsRequest.Builder batchObjectsRequestBuilder = WeaviateProtoBatch.BatchObjectsRequest.newBuilder();
    batchObjectsRequestBuilder.addAllObjects(batchObjects);
    Optional.ofNullable(consistencyLevel)
      .map(cl -> {
        switch (cl) {
          case ConsistencyLevel.ALL:
            return WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ALL;
          case ConsistencyLevel.QUORUM:
            return WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_QUORUM;
          default:
            return WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE;
        }
      }).ifPresent(batchObjectsRequestBuilder::setConsistencyLevel);
    WeaviateProtoBatch.BatchObjectsRequest batchObjectsRequest = batchObjectsRequestBuilder.build();


    // TODO convert ListenableFuture into CompletableFuture?
    return CompletableFuture.supplyAsync(() -> {
      AsyncGrpcClient grpcClient = AsyncGrpcClient.create(config, tokenProvider);
      try {
        return grpcClient.batchObjects(batchObjectsRequest).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new CompletionException(e);
      } finally {
        grpcClient.shutdown();
      }
    }).thenApply(batchObjectsReply -> {
      List<WeaviateErrorMessage> weaviateErrorMessages = batchObjectsReply.getErrorsList().stream()
        .map(WeaviateProtoBatch.BatchObjectsReply.BatchError::getError)
        .filter(e -> !e.isEmpty())
        .map(msg -> WeaviateErrorMessage.builder().message(msg).build())
        .collect(Collectors.toList());

      if (!weaviateErrorMessages.isEmpty()) {
        int statusCode = HttpStatus.SC_UNPROCESSABLE_CONTENT;
        WeaviateErrorResponse weaviateErrorResponse = WeaviateErrorResponse.builder()
          .code(statusCode)
          .message(StringUtils.join(weaviateErrorMessages, ","))
          .error(weaviateErrorMessages)
          .build();
        return new Result<>(statusCode, null, weaviateErrorResponse);
      }

      ObjectGetResponse[] objectGetResponses = batch.stream().map(o -> {
        ObjectsGetResponseAO2Result result = new ObjectsGetResponseAO2Result();
        result.setStatus(ObjectGetResponseStatus.SUCCESS);

        ObjectGetResponse resp = new ObjectGetResponse();
        resp.setId(o.getId());
        resp.setClassName(o.getClassName());
        resp.setTenant(o.getTenant());
        resp.setResult(result);
        return resp;
      }).toArray(ObjectGetResponse[]::new);

      return new Result<>(HttpStatus.SC_OK, objectGetResponses, null);
    });
  }

  private CompletableFuture<Result<ObjectGetResponse[]>> internalHttpRun(List<WeaviateObject> batch) {
    System.out.println("batcher internalHttpRun started");

    CompletableFuture<Result<ObjectGetResponse[]>> future = new CompletableFuture<>();
    ObjectsBatchRequestBody payload = ObjectsBatchRequestBody.builder()
      .objects(batch.toArray(new WeaviateObject[0]))
      .fields(new String[]{"ALL"})
      .build();
    String path = objectsPath.buildCreate(ObjectsPath.Params.builder()
      .consistencyLevel(consistencyLevel)
      .build());
    System.out.println("batcher internalHttpRun sendPostRequest");
    sendPostRequest(path, payload, ObjectGetResponse[].class, new FutureCallback<Result<ObjectGetResponse[]>>() {
      @Override
      public void completed(Result<ObjectGetResponse[]> batchResult) {
        System.out.println("batcher internalHttpRun sendPostRequest completed");

        future.complete(batchResult);
      }

      @Override
      public void failed(Exception e) {

        System.out.println("batcher internalHttpRun sendPostRequest failed");

        future.completeExceptionally(e);
      }

      @Override
      public void cancelled() {
        System.out.println("batcher internalHttpRun sendPostRequest cancelled");

      }
    });
    return future;
  }

  private Pair<List<ObjectGetResponse>, List<WeaviateObject>> fetchCreatedAndBuildBatchToReRun(List<WeaviateObject> batch) {
    List<WeaviateObject> rerunBatch = new ArrayList<>(batch.size());
    List<ObjectGetResponse> createdResponses = new ArrayList<>(batch.size());
    List<CompletableFuture<Result<List<WeaviateObject>>>> futures = new ArrayList<>(batch.size());

    System.out.println("batcher fetchCreatedAndBuildBatchToReRun started");

    for (WeaviateObject batchObject : batch) {
      futures.add(fetchExistingObject(batchObject));
    }
    System.out.println("batcher fetchCreatedAndBuildBatchToReRun futures created");

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((v, t) -> {
      System.out.println("batcher fetchCreatedAndBuildBatchToReRun when complete");
      try {
        for (int i = 0; i < batch.size(); i++) {
          System.out.printf("batcher fetchCreatedAndBuildBatchToReRun loop %d\n", i);

          CompletableFuture<Result<List<WeaviateObject>>> future = futures.get(i);
          WeaviateObject batchObject = batch.get(i);

          System.out.printf("batcher fetchCreatedAndBuildBatchToReRun loop %d, batch object\n", i);

          if (future.isCompletedExceptionally()) {
            System.out.printf("batcher fetchCreatedAndBuildBatchToReRun loop %d, isCompletedExceptionally\n", i);
            rerunBatch.add(batchObject);
            continue;
          }

          Result<List<WeaviateObject>> existingResult = future.get();
          if (existingResult.hasErrors() || ObjectUtils.isEmpty(existingResult.getResult())) {
            System.out.printf("batcher fetchCreatedAndBuildBatchToReRun loop %d, has errors || empty\n", i);
            rerunBatch.add(batchObject);
            continue;
          }

          WeaviateObject existingObject = existingResult.getResult().get(0);
          if (isDifferentObject(batchObject, existingObject)) {
            System.out.printf("batcher fetchCreatedAndBuildBatchToReRun loop %d, is different\n", i);
            rerunBatch.add(batchObject);
            continue;
          }

          System.out.printf("batcher fetchCreatedAndBuildBatchToReRun loop %d, createResponseFromExistingObject\n", i);
          createdResponses.add(createResponseFromExistingObject(existingObject));
        }
      } catch (InterruptedException | ExecutionException e) {
        throw new CompletionException(e);
      }
    }).join();
    System.out.println("batcher fetchCreatedAndBuildBatchToReRun join");

    return Pair.of(createdResponses, rerunBatch);
  }

  private CompletableFuture<Result<List<WeaviateObject>>> fetchExistingObject(WeaviateObject batchObject) {
    System.out.printf("batcher fetchExistingObject started [%s]\n", batchObject.getId());

    CompletableFuture<Result<List<WeaviateObject>>> future = new CompletableFuture<>();
    data.objectsGetter()
      .withID(batchObject.getId())
      .withClassName(batchObject.getClassName())
      .withVector()
      .run(new FutureCallback<Result<List<WeaviateObject>>>() {
        @Override
        public void completed(Result<List<WeaviateObject>> objectsResult) {

          System.out.printf("batcher fetchExistingObject completed [%s]\n -> result [%s]\n", batchObject.getId(), objectsResult);
          future.complete(objectsResult);
        }

        @Override
        public void failed(Exception e) {

          System.out.printf("batcher fetchExistingObject failed [%s]\n -> exception [%s]\n", batchObject.getId(), e);
          future.completeExceptionally(e);
        }

        @Override
        public void cancelled() {
          System.out.printf("batcher fetchExistingObject cancelled [%s]\n", batchObject.getId());

        }
      });

    System.out.printf("batcher fetchExistingObject finished [%s]\n", batchObject.getId());

    return future;
  }

  private boolean isDifferentObject(WeaviateObject batchObject, WeaviateObject existingObject) {
    if ((existingObject.getVector() != null || batchObject.getVector() != null)
      && !Arrays.equals(existingObject.getVector(), batchObject.getVector())
    ) {
      return true;
    }

    Map<String, Object> existingProperties = existingObject.getProperties();
    Map<String, Object> batchProperties = batchObject.getProperties();

    if ((existingProperties != null && batchProperties == null)
      || (existingProperties == null && batchProperties != null)
    ) {
      return true;
    }

    if (existingProperties != null && !existingProperties.equals(batchProperties)) {
      // TODO improve as lists will always be !=
      return true;
    }

    return false;
  }

  private ObjectGetResponse createResponseFromExistingObject(WeaviateObject existingObject) {
    ObjectsGetResponseAO2Result result = new ObjectsGetResponseAO2Result();
    result.setStatus(ObjectGetResponseStatus.SUCCESS);

    ObjectGetResponse response = new ObjectGetResponse();
    response.setId(existingObject.getId());
    response.setClassName(existingObject.getClassName());
    response.setProperties(existingObject.getProperties());
    response.setAdditional(existingObject.getAdditional());
    response.setCreationTimeUnix(existingObject.getCreationTimeUnix());
    response.setLastUpdateTimeUnix(existingObject.getLastUpdateTimeUnix());
    response.setVector(existingObject.getVector());
    response.setVectorWeights(existingObject.getVectorWeights());
    response.setResult(result);

    return response;
  }


  private List<ObjectGetResponse> combineSingleResponses(List<ObjectGetResponse> combinedSingleResponses,
                                                         List<ObjectGetResponse> createdResponses) {
    if (ObjectUtils.isNotEmpty(createdResponses)) {
      combinedSingleResponses = ObjectUtils.isEmpty(combinedSingleResponses)
        ? createdResponses
        : Stream.of(combinedSingleResponses, createdResponses)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    }

    return combinedSingleResponses;
  }

  private Result<ObjectGetResponse[]> createFinalResultFromLastResultAndCombinedSingleResponses(Result<ObjectGetResponse[]> lastResult,
                                                                                                Throwable throwable,
                                                                                                List<ObjectGetResponse> combinedSingleResponses,
                                                                                                List<WeaviateObject> failedBatch) {
    System.out.println(lastResult);
    System.out.println(throwable);
    System.out.println(combinedSingleResponses);
    System.out.println(failedBatch);

    int statusCode = 0;
    if (throwable != null && lastResult == null) {
      lastResult = new Result<>(statusCode, null, WeaviateErrorResponse.builder()
        .error(Collections.singletonList(WeaviateErrorMessage.builder()
          .message(throwable.getMessage())
          .throwable(throwable)
          .build()))
        .code(statusCode)
        .build()
      );
    }

    if (ObjectUtils.isEmpty(failedBatch) && ObjectUtils.isEmpty(combinedSingleResponses)) {
      return lastResult;
    }

    ObjectGetResponse[] allResponses = null;
    if (ObjectUtils.isNotEmpty(lastResult.getResult())) {
      allResponses = lastResult.getResult();
    }
    if (ObjectUtils.isNotEmpty(combinedSingleResponses)) {
      allResponses = ArrayUtils.addAll(allResponses, combinedSingleResponses.toArray(new ObjectGetResponse[0]));
    }

    if (ObjectUtils.isEmpty(failedBatch)) {
      return new Result<>(statusCode, allResponses, null);
    }

    String failedIds = failedBatch.stream().map(WeaviateObject::getId).collect(Collectors.joining(", "));
    WeaviateErrorMessage failedIdsMessage = WeaviateErrorMessage.builder().message("Failed ids: " + failedIds).build();
    List<WeaviateErrorMessage> messages;

    if (lastResult.hasErrors()) {
      statusCode = lastResult.getError().getStatusCode();
      List<WeaviateErrorMessage> prevMessages = lastResult.getError().getMessages();
      messages = new ArrayList<>(prevMessages.size() + 1);
      messages.addAll(prevMessages);
      messages.add(failedIdsMessage);
    } else {
      messages = Collections.singletonList(failedIdsMessage);
    }

    return new Result<>(statusCode, allResponses, WeaviateErrorResponse.builder()
      .error(messages)
      .code(statusCode)
      .build()
    );
  }

  @Getter
  @Builder
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
  public static class BatchRetriesConfig {

    public static final int MAX_TIMEOUT_RETRIES = 3;
    public static final int MAX_CONNECTION_RETRIES = 3;
    public static final int RETRIES_INTERVAL = 2000;

    int maxTimeoutRetries;
    int maxConnectionRetries;
    int retriesIntervalMs;

    private BatchRetriesConfig(int maxTimeoutRetries, int maxConnectionRetries, int retriesIntervalMs) {
      Assert.requireGreaterEqual(maxTimeoutRetries, 0, "maxTimeoutRetries");
      Assert.requireGreaterEqual(maxConnectionRetries, 0, "maxConnectionRetries");
      Assert.requireGreater(retriesIntervalMs, 0, "retriesIntervalMs");

      this.maxTimeoutRetries = maxTimeoutRetries;
      this.maxConnectionRetries = maxConnectionRetries;
      this.retriesIntervalMs = retriesIntervalMs;
    }

    public static ObjectsBatcher.BatchRetriesConfig.BatchRetriesConfigBuilder defaultConfig() {
      return ObjectsBatcher.BatchRetriesConfig.builder()
        .maxTimeoutRetries(MAX_TIMEOUT_RETRIES)
        .maxConnectionRetries(MAX_CONNECTION_RETRIES)
        .retriesIntervalMs(RETRIES_INTERVAL);
    }
  }

  @Getter
  @Builder
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
  public static class AutoBatchConfig {

    public static final int BATCH_SIZE = 100;

    int batchSize;
    Consumer<Result<ObjectGetResponse[]>> callback;

    private AutoBatchConfig(int batchSize, Consumer<Result<ObjectGetResponse[]>> callback) {
      Assert.requireGreaterEqual(batchSize, 1, "batchSize");

      this.batchSize = batchSize;
      this.callback = callback;
    }

    public static ObjectsBatcher.AutoBatchConfig.AutoBatchConfigBuilder defaultConfig() {
      return ObjectsBatcher.AutoBatchConfig.builder()
        .batchSize(BATCH_SIZE)
        .callback(null);
    }
  }
}
