package technology.semi.weaviate.client.v1.batch.api;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.impl.BHttpConnectionBase;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.base.WeaviateErrorResponse;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.util.Assert;
import technology.semi.weaviate.client.v1.batch.model.BatchReference;
import technology.semi.weaviate.client.v1.batch.model.BatchReferenceResponse;

import java.io.Closeable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReferencesBatcher extends BaseClient<BatchReferenceResponse[]>
  implements ClientResult<BatchReferenceResponse[]>, Closeable {

  private final BatchRetriesConfig batchRetriesConfig;
  private final AutoBatchConfig autoBatchConfig;
  private final boolean autoRunEnabled;
  private final ScheduledExecutorService executorService;
  private final DelayedExecutor<?> delayedExecutor;
  private final List<BatchReference> references;
  private final List<CompletableFuture<Result<BatchReferenceResponse[]>>> undoneFutures;


  private ReferencesBatcher(HttpClient httpClient, Config config, BatchRetriesConfig batchRetriesConfig, AutoBatchConfig autoBatchConfig) {
    super(httpClient, config);
    this.references = new ArrayList<>();
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

  public static ReferencesBatcher create(HttpClient httpClient, Config config) {
    return new ReferencesBatcher(httpClient, config, BatchRetriesConfig.defaultConfig().build(), null);
  }

  public static ReferencesBatcher create(HttpClient httpClient, Config config, BatchRetriesConfig batchRetriesConfig) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    return new ReferencesBatcher(httpClient, config, batchRetriesConfig, null);
  }

  public static ReferencesBatcher createAuto(HttpClient httpClient, Config config) {
    return new ReferencesBatcher(httpClient, config, BatchRetriesConfig.defaultConfig().build(), AutoBatchConfig.defaultConfig().build());
  }

  public static ReferencesBatcher createAuto(HttpClient httpClient, Config config, BatchRetriesConfig batchRetriesConfig, AutoBatchConfig autoBatchConfig) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    Assert.requiredNotNull(autoBatchConfig, "autoBatchConfig");
    return new ReferencesBatcher(httpClient, config, batchRetriesConfig, autoBatchConfig);
  }


  public ReferencesBatcher withReference(BatchReference reference) {
    return withReferences(reference);
  }

  public ReferencesBatcher withReferences(BatchReference... references) {
    this.references.addAll(Arrays.asList(references));
    autoRun();
    return this;
  }

  @Override
  public Result<BatchReferenceResponse[]> run() {
    if (autoRunEnabled) {
      flush();  // fallback to flush in auto run enabled
      return null;
    }

    if (references.isEmpty()) {
      return new Result<>(0, new BatchReferenceResponse[0], null);
    }

    List<BatchReference> batch = extractBatch(references.size());
    return runRecursively(batch, 0, 0,
      (DelayedExecutor<Result<BatchReferenceResponse[]>>) delayedExecutor);
  }

  public void flush() {
    if (!autoRunEnabled) {
      run();  // fallback to run if auto run disabled
      return;
    }

    if (!references.isEmpty()) {
      List<BatchReference> batch = extractBatch(references.size());
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

  private List<BatchReference> extractBatch(int batchSize) {
    List<BatchReference> batch = new ArrayList<>(batchSize);
    List<BatchReference> sublist = references.subList(0, batchSize);

    batch.addAll(sublist);
    sublist.clear();

    return batch;
  }

  private void autoRun() {
    if (!autoRunEnabled) {
      return;
    }

    while (references.size() >= autoBatchConfig.batchSize) {
      List<BatchReference> batch = extractBatch(autoBatchConfig.batchSize);
      runInThread(batch);
    }
  }

  private void runInThread(List<BatchReference> batch) {
    CompletableFuture<Result<BatchReferenceResponse[]>> future = CompletableFuture.supplyAsync(
      () -> createRunFuture(batch),
      executorService
    ).thenCompose(f -> f);

    if (autoBatchConfig.callback != null) {
      future = future.whenComplete((result, e) -> autoBatchConfig.callback.accept(result));
    }

    CompletableFuture<Result<BatchReferenceResponse[]>> undoneFuture = future;
    undoneFutures.add(undoneFuture);
    undoneFuture.whenComplete((result, ex) -> undoneFutures.remove(undoneFuture));
  }

  private CompletableFuture<Result<BatchReferenceResponse[]>> createRunFuture(List<BatchReference> batch) {
    return runRecursively(batch, 0, 0,
      (DelayedExecutor<CompletableFuture<Result<BatchReferenceResponse[]>>>) delayedExecutor);
  }

  private <T> T runRecursively(List<BatchReference> batch, int connectionErrorCount, int timeoutErrorCount,
                               DelayedExecutor<T> delayedExecutor) {
    Result<BatchReferenceResponse[]> result = internalRun(batch);

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
          if (timeoutErrorCount++ < batchRetriesConfig.maxTimeoutRetries) {
            executeAgain = true;
            delay = timeoutErrorCount * batchRetriesConfig.retriesIntervalMs;
          }
        }

        if (executeAgain) {
          int lambdaConnectionErrorCount = connectionErrorCount;
          int lambdaTimeoutErrorCount = timeoutErrorCount;
          List<BatchReference> lambdaBatch = batch;

          return delayedExecutor.delayed(
            delay,
            () -> runRecursively(lambdaBatch, lambdaConnectionErrorCount, lambdaTimeoutErrorCount, delayedExecutor)
          );
        }
      }
    } else {
      batch = null;
    }

    Result<BatchReferenceResponse[]> finalResult = createFinalResultFromLastResult(result, batch);
    return delayedExecutor.now(finalResult);
  }

  private Result<BatchReferenceResponse[]> internalRun(List<BatchReference> batch) {
    BatchReference[] payload = batch.toArray(new BatchReference[0]);
    Response<BatchReferenceResponse[]> resp = sendPostRequest("/batch/references", payload, BatchReferenceResponse[].class);
    return new Result<>(resp);
  }

  private Result<BatchReferenceResponse[]> createFinalResultFromLastResult(
    Result<BatchReferenceResponse[]> lastResult, List<BatchReference> failedBatch) {

    if (ObjectUtils.isEmpty(failedBatch)) {
      return lastResult;
    }

    String failedRefs = failedBatch.stream()
      .map(ref -> ref.getFrom() + " => " + ref.getTo())
      .collect(Collectors.joining(", "));
    WeaviateErrorMessage failedRefsMessage = WeaviateErrorMessage.builder().message("Failed refs: " + failedRefs).build();
    List<WeaviateErrorMessage> messages;

    int statusCode = 0;
    if (lastResult.hasErrors()) {
      statusCode = lastResult.getError().getStatusCode();
      List<WeaviateErrorMessage> prevMessages = lastResult.getError().getMessages();
      messages = new ArrayList<>(prevMessages.size() + 1);
      messages.addAll(prevMessages);
      messages.add(failedRefsMessage);
    } else {
      messages = Collections.singletonList(failedRefsMessage);
    }

    return new Result<>(statusCode, null, WeaviateErrorResponse.builder()
      .error(messages)
      .code(statusCode)
      .build()
    );
  }



  private interface DelayedExecutor<T> {
    T delayed(int delay, Supplier<T> supplier);
    T now(Result<BatchReferenceResponse[]> result);
  }

  @RequiredArgsConstructor
  private static class ExecutorServiceDelayedExecutor implements DelayedExecutor<CompletableFuture<Result<BatchReferenceResponse[]>>> {

    private final ScheduledExecutorService executorService;

    @Override
    public CompletableFuture<Result<BatchReferenceResponse[]>> delayed(int delay, Supplier<CompletableFuture<Result<BatchReferenceResponse[]>>> supplier) {
      Executor executor = (runnable) -> executorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
      return CompletableFuture.supplyAsync(supplier, executor).thenCompose(f -> f);
    }

    @Override
    public CompletableFuture<Result<BatchReferenceResponse[]>> now(Result<BatchReferenceResponse[]> result) {
      return CompletableFuture.completedFuture(result);
    }
  }


  private static class SleepDelayedExecutor implements DelayedExecutor<Result<BatchReferenceResponse[]>> {

    @Override
    public Result<BatchReferenceResponse[]> delayed(int delay, Supplier<Result<BatchReferenceResponse[]>> supplier) {
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return supplier.get();
    }

    @Override
    public Result<BatchReferenceResponse[]> now(Result<BatchReferenceResponse[]> result) {
      return result;
    }
  }

  @Getter
  @Builder
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
  @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
  public static class AutoBatchConfig {

    public static final int BATCH_SIZE = 100;
    public static final int POOL_SIZE = 1;
    public static final int AWAIT_TERMINATION_MS = 10_000;

    int batchSize;
    int poolSize;
    int awaitTerminationMs;
    Consumer<Result<BatchReferenceResponse[]>> callback;

    private AutoBatchConfig(int batchSize, int poolSize, int awaitTerminationMs,
                            Consumer<Result<BatchReferenceResponse[]>> callback) {
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
