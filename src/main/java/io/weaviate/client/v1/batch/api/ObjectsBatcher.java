package io.weaviate.client.v1.batch.api;

import java.io.Closeable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.grpc.GrpcClient;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.Assert;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply.BatchError;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.batch.grpc.BatchObjectConverter;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponseStatus;
import io.weaviate.client.v1.batch.model.ObjectsBatchRequestBody;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result.ErrorResponse;
import io.weaviate.client.v1.batch.util.ObjectsPath;
import io.weaviate.client.v1.data.Data;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

public class ObjectsBatcher extends BaseClient<ObjectGetResponse[]>
    implements ClientResult<ObjectGetResponse[]>, Closeable {

  private final Data data;
  private final ObjectsPath objectsPath;

  private final BatchRetriesConfig batchRetriesConfig;
  private final AutoBatchConfig autoBatchConfig;
  private final boolean autoRunEnabled;
  private final ScheduledExecutorService executorService;
  private final DelayedExecutor<?> delayedExecutor;
  private final List<WeaviateObject> objects;
  private String consistencyLevel;
  private final List<CompletableFuture<Result<ObjectGetResponse[]>>> undoneFutures;
  private final boolean useGRPC;
  private final AccessTokenProvider tokenProvider;
  private final GrpcVersionSupport grpcVersionSupport;
  private final Config config;

  private ObjectsBatcher(HttpClient httpClient, Config config, Data data, ObjectsPath objectsPath,
      AccessTokenProvider tokenProvider, GrpcVersionSupport grpcVersionSupport,
      BatchRetriesConfig batchRetriesConfig, AutoBatchConfig autoBatchConfig) {
    super(httpClient, config);
    this.config = config;
    this.useGRPC = config.useGRPC();
    this.tokenProvider = tokenProvider;
    this.data = data;
    this.objectsPath = objectsPath;
    this.grpcVersionSupport = grpcVersionSupport;
    this.objects = new ArrayList<>();
    this.batchRetriesConfig = batchRetriesConfig;

    if (autoBatchConfig != null) {
      this.autoRunEnabled = true;
      this.autoBatchConfig = autoBatchConfig;
      this.executorService = Executors.newScheduledThreadPool(autoBatchConfig.poolSize);
      this.delayedExecutor = new ExecutorServiceDelayedExecutor(executorService);
      this.undoneFutures = Collections.synchronizedList(new ArrayList<>());
    } else {
      this.autoRunEnabled = false;
      this.autoBatchConfig = null;
      this.executorService = null;
      this.delayedExecutor = new SleepDelayedExecutor();
      this.undoneFutures = null;
    }
  }

  public static ObjectsBatcher create(HttpClient httpClient, Config config, Data data, ObjectsPath objectsPath,
      AccessTokenProvider tokenProvider, GrpcVersionSupport grpcVersionSupport,
      BatchRetriesConfig batchRetriesConfig) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    return new ObjectsBatcher(httpClient, config, data, objectsPath, tokenProvider, grpcVersionSupport,
        batchRetriesConfig, null);
  }

  public static ObjectsBatcher createAuto(HttpClient httpClient, Config config, Data data, ObjectsPath objectsPath,
      AccessTokenProvider tokenProvider, GrpcVersionSupport grpcVersionSupport,
      BatchRetriesConfig batchRetriesConfig, AutoBatchConfig autoBatchConfig) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    Assert.requiredNotNull(autoBatchConfig, "autoBatchConfig");
    return new ObjectsBatcher(httpClient, config, data, objectsPath, tokenProvider, grpcVersionSupport,
        batchRetriesConfig, autoBatchConfig);
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

  @Override
  public Result<ObjectGetResponse[]> run() {
    if (autoRunEnabled) {
      flush(); // fallback to flush in auto run enabled
      return null;
    }

    if (objects.isEmpty()) {
      return new Result<>(0, new ObjectGetResponse[0], null);
    }

    List<WeaviateObject> batch = extractBatch(objects.size());
    return runRecursively(batch, 0, 0, null,
        (DelayedExecutor<Result<ObjectGetResponse[]>>) delayedExecutor);
  }

  public void flush() {
    if (!autoRunEnabled) {
      run(); // fallback to run if auto run disabled
      return;
    }

    if (!objects.isEmpty()) {
      List<WeaviateObject> batch = extractBatch(objects.size());
      runInThread(batch);
    }

    CompletableFuture<?>[] futures = undoneFutures.toArray(new CompletableFuture[0]);
    if (futures.length == 0) {
      return;
    }

    CompletableFuture.allOf(futures).join();
  }

  @Override
  public void close() {
    if (!autoRunEnabled) {
      return;
    }

    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(autoBatchConfig.awaitTerminationMs, TimeUnit.MILLISECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
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
      runInThread(batch);
    }
  }

  private void runInThread(List<WeaviateObject> batch) {
    CompletableFuture<Result<ObjectGetResponse[]>> future = CompletableFuture.supplyAsync(
        () -> createRunFuture(batch),
        executorService).thenCompose(f -> f);

    if (autoBatchConfig.callback != null) {
      future = future.whenComplete((result, e) -> autoBatchConfig.callback.accept(result));
    }

    CompletableFuture<Result<ObjectGetResponse[]>> undoneFuture = future;
    undoneFutures.add(undoneFuture);
    undoneFuture.whenComplete((result, ex) -> undoneFutures.remove(undoneFuture));
  }

  private CompletableFuture<Result<ObjectGetResponse[]>> createRunFuture(List<WeaviateObject> batch) {
    return runRecursively(batch, 0, 0, null,
        (DelayedExecutor<CompletableFuture<Result<ObjectGetResponse[]>>>) delayedExecutor);
  }

  private <T> T runRecursively(List<WeaviateObject> batch, int connectionErrorCount, int timeoutErrorCount,
      List<ObjectGetResponse> combinedSingleResponses, DelayedExecutor<T> delayedExecutor) {
    Result<ObjectGetResponse[]> result = useGRPC ? internalGrpcRun(batch) : internalRun(batch);

    if (result.hasErrors()) {
      List<WeaviateErrorMessage> messages = result.getError().getMessages();
      if (!messages.isEmpty()) {
        Throwable throwable = messages.get(0).getThrowable();
        boolean executeAgain = false;
        int delay = 0;

        if (throwable instanceof ConnectException) {
          if (connectionErrorCount++ < batchRetriesConfig.maxConnectionRetries) {
            executeAgain = true;
            delay = connectionErrorCount * batchRetriesConfig.retriesIntervalMs;
          }
        } else if (throwable instanceof SocketTimeoutException) {
          Pair<List<ObjectGetResponse>, List<WeaviateObject>> pair = fetchCreatedAndBuildBatchToReRun(batch);
          combinedSingleResponses = combineSingleResponses(combinedSingleResponses, pair.getLeft());
          batch = pair.getRight();

          if (ObjectUtils.isNotEmpty(batch) && timeoutErrorCount++ < batchRetriesConfig.maxTimeoutRetries) {
            executeAgain = true;
            delay = timeoutErrorCount * batchRetriesConfig.retriesIntervalMs;
          }
        }

        if (executeAgain) {
          int lambdaConnectionErrorCount = connectionErrorCount;
          int lambdaTimeoutErrorCount = timeoutErrorCount;
          List<WeaviateObject> lambdaBatch = batch;
          List<ObjectGetResponse> lambdaCombinedSingleResponses = combinedSingleResponses;

          return delayedExecutor.delayed(
              delay,
              () -> runRecursively(lambdaBatch, lambdaConnectionErrorCount, lambdaTimeoutErrorCount,
                  lambdaCombinedSingleResponses, delayedExecutor));
        }
      }
    } else {
      batch = null;
    }

    Result<ObjectGetResponse[]> finalResult = createFinalResultFromLastResultAndCombinedSingleResponses(result,
        combinedSingleResponses, batch);
    return delayedExecutor.now(finalResult);
  }

  private Result<ObjectGetResponse[]> internalRun(List<WeaviateObject> batch) {
    ObjectsBatchRequestBody batchRequest = ObjectsBatchRequestBody.builder()
        .objects(batch.toArray(new WeaviateObject[batch.size()]))
        .fields(new String[] { "ALL" })
        .build();
    String path = objectsPath.buildCreate(ObjectsPath.Params.builder()
        .consistencyLevel(consistencyLevel)
        .build());
    Response<ObjectGetResponse[]> resp = sendPostRequest(path, batchRequest, ObjectGetResponse[].class);
    return new Result<>(resp);
  }

  private Result<ObjectGetResponse[]> internalGrpcRun(List<WeaviateObject> batch) {
    BatchObjectConverter batchObjectConverter = new BatchObjectConverter(grpcVersionSupport);
    List<WeaviateProtoBatch.BatchObject> batchObjects = batch.stream()
        .map(batchObjectConverter::toBatchObject)
        .collect(Collectors.toList());
    WeaviateProtoBatch.BatchObjectsRequest.Builder batchObjectsRequestBuilder = WeaviateProtoBatch.BatchObjectsRequest
        .newBuilder();
    batchObjectsRequestBuilder.addAllObjects(batchObjects);
    if (consistencyLevel != null) {
      WeaviateProtoBase.ConsistencyLevel cl = WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE;
      if (consistencyLevel.equals(ConsistencyLevel.ALL)) {
        cl = WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ALL;
      }
      if (consistencyLevel.equals(ConsistencyLevel.QUORUM)) {
        cl = WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_QUORUM;
      }
      batchObjectsRequestBuilder.setConsistencyLevel(cl);
    }

    WeaviateProtoBatch.BatchObjectsRequest batchObjectsRequest = batchObjectsRequestBuilder.build();
    WeaviateProtoBatch.BatchObjectsReply batchObjectsReply;
    GrpcClient grpcClient = GrpcClient.create(this.config, this.tokenProvider);
    try {
      batchObjectsReply = grpcClient.batchObjects(batchObjectsRequest);
    } finally {
      grpcClient.shutdown();
    }
    return resultFromBatchObjectsReply(batchObjectsReply, batch);
  }

  public static Result<ObjectGetResponse[]> resultFromBatchObjectsReply(BatchObjectsReply reply,
      List<WeaviateObject> batch) {
    Map<Integer, String> errors = reply.getErrorsList()
        .stream().collect(Collectors.toMap(BatchError::getIndex, BatchError::getError));
    List<WeaviateErrorMessage> errorMessages = new ArrayList<>();
    WeaviateErrorResponse responseError = null;
    int responseCode = HttpStatus.SC_SUCCESS;

    ObjectGetResponse[] responseObjects = new ObjectGetResponse[batch.size()];
    for (int i = 0; i < responseObjects.length; i++) {
      ObjectGetResponse r = new ObjectGetResponse();
      ObjectsGetResponseAO2Result insertResult = new ObjectsGetResponseAO2Result();
      if (errors.containsKey(i)) {
        insertResult.setStatus(ObjectGetResponseStatus.FAILED);
        insertResult.setErrors(new ErrorResponse(errors.get(i)));

        errorMessages.add(WeaviateErrorMessage.builder().message(errors.get(i)).build());
      } else {
        insertResult.setStatus(ObjectGetResponseStatus.SUCCESS);

        WeaviateObject batchObject = batch.get(i);
        r.setId(batchObject.getId());
        r.setClassName(batchObject.getClassName());
        r.setTenant(batchObject.getTenant());
        r.setVector(batchObject.getVector());
        r.setVectors(batchObject.getVectors());
        r.setMultiVectors(batchObject.getMultiVectors());
      }
      r.setResult(insertResult);
      responseObjects[i] = r;
    }

    if (!errors.isEmpty()) {
      responseCode = HttpStatus.SC_UNPROCESSABLE_CONTENT;

      // An important distinction between internalGrpcRun and internalRun
      // is that the regular batching (non-gRPC) method will not surface
      // an error on the "response level" and only report errors on the
      // object level.
      //
      // Because previously internalGrpcRun used to return 422 and a
      // WeaviateErrorResponse on partial errors too, we preserve this
      // behavior for b/c.
      responseError = WeaviateErrorResponse.builder()
          .code(responseCode)
          .message(StringUtils.join(errors.values(), ","))
          .error(errorMessages).build();
    }
    return new Result<>(responseCode, responseObjects, responseError);
  }

  private Pair<List<ObjectGetResponse>, List<WeaviateObject>> fetchCreatedAndBuildBatchToReRun(
      List<WeaviateObject> batch) {
    List<WeaviateObject> rerunBatch = new ArrayList<>(batch.size());
    List<ObjectGetResponse> createdResponses = new ArrayList<>(batch.size());

    for (WeaviateObject batchObject : batch) {
      Result<List<WeaviateObject>> existingResult = fetchExistingObject(batchObject);

      if (existingResult.hasErrors() || ObjectUtils.isEmpty(existingResult.getResult())) {
        rerunBatch.add(batchObject);
        continue;
      }

      WeaviateObject existingObject = existingResult.getResult().get(0);
      if (isDifferentObject(batchObject, existingObject)) {
        rerunBatch.add(batchObject);
        continue;
      }

      createdResponses.add(createResponseFromExistingObject(existingObject));
    }

    return Pair.of(createdResponses, rerunBatch);
  }

  private Result<List<WeaviateObject>> fetchExistingObject(WeaviateObject batchObject) {
    return data.objectsGetter()
        .withID(batchObject.getId())
        .withClassName(batchObject.getClassName())
        .withVector()
        .run();
  }

  private boolean isDifferentObject(WeaviateObject batchObject, WeaviateObject existingObject) {
    if ((existingObject.getVector() != null || batchObject.getVector() != null)
        && !Arrays.equals(existingObject.getVector(), batchObject.getVector())) {
      return true;
    }

    Map<String, Object> existingProperties = existingObject.getProperties();
    Map<String, Object> batchProperties = batchObject.getProperties();

    if ((existingProperties != null && batchProperties == null)
        || (existingProperties == null && batchProperties != null)) {
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
    response.setVectors(existingObject.getVectors());
    response.setMultiVectors(existingObject.getMultiVectors());
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

  private Result<ObjectGetResponse[]> createFinalResultFromLastResultAndCombinedSingleResponses(
      Result<ObjectGetResponse[]> lastResult, List<ObjectGetResponse> combinedSingleResponses,
      List<WeaviateObject> failedBatch) {

    if (ObjectUtils.isEmpty(failedBatch) && ObjectUtils.isEmpty(combinedSingleResponses)) {
      return lastResult;
    }

    int statusCode = 0;
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
        .build());
  }

  private interface DelayedExecutor<T> {
    T delayed(int delay, Supplier<T> supplier);

    T now(Result<ObjectGetResponse[]> result);
  }

  @RequiredArgsConstructor
  private static class ExecutorServiceDelayedExecutor
      implements DelayedExecutor<CompletableFuture<Result<ObjectGetResponse[]>>> {

    private final ScheduledExecutorService executorService;

    @Override
    public CompletableFuture<Result<ObjectGetResponse[]>> delayed(int delay,
        Supplier<CompletableFuture<Result<ObjectGetResponse[]>>> supplier) {
      Executor executor = (runnable) -> executorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
      return CompletableFuture.supplyAsync(supplier, executor).thenCompose(f -> f);
    }

    @Override
    public CompletableFuture<Result<ObjectGetResponse[]>> now(Result<ObjectGetResponse[]> result) {
      return CompletableFuture.completedFuture(result);
    }
  }

  private static class SleepDelayedExecutor implements DelayedExecutor<Result<ObjectGetResponse[]>> {

    @Override
    public Result<ObjectGetResponse[]> delayed(int delay, Supplier<Result<ObjectGetResponse[]>> supplier) {
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return supplier.get();
    }

    @Override
    public Result<ObjectGetResponse[]> now(Result<ObjectGetResponse[]> result) {
      return result;
    }
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

    public static BatchRetriesConfigBuilder defaultConfig() {
      return BatchRetriesConfig.builder()
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
    public static final int POOL_SIZE = 1;
    public static final int AWAIT_TERMINATION_MS = 10_000;

    int batchSize;
    int poolSize;
    int awaitTerminationMs;
    Consumer<Result<ObjectGetResponse[]>> callback;

    private AutoBatchConfig(int batchSize, int poolSize, int awaitTerminationMs,
        Consumer<Result<ObjectGetResponse[]>> callback) {
      Assert.requireGreaterEqual(batchSize, 1, "batchSize");
      Assert.requireGreaterEqual(poolSize, 1, "corePoolSize");
      Assert.requireGreater(awaitTerminationMs, 0, "awaitTerminationMs");

      this.batchSize = batchSize;
      this.poolSize = poolSize;
      this.awaitTerminationMs = awaitTerminationMs;
      this.callback = callback;
    }

    public static AutoBatchConfigBuilder defaultConfig() {
      return AutoBatchConfig.builder()
          .batchSize(BATCH_SIZE)
          .poolSize(POOL_SIZE)
          .awaitTerminationMs(AWAIT_TERMINATION_MS)
          .callback(null);
    }
  }
}
