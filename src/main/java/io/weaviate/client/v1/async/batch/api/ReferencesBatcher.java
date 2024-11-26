package io.weaviate.client.v1.async.batch.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.util.Assert;
import io.weaviate.client.base.util.Futures;
import io.weaviate.client.v1.batch.model.BatchReference;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;
import io.weaviate.client.v1.batch.util.ReferencesPath;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReferencesBatcher extends AsyncBaseClient<BatchReferenceResponse[]>
  implements AsyncClientResult<BatchReferenceResponse[]> {

  private final ReferencesPath referencesPath;

  private final BatchRetriesConfig batchRetriesConfig;
  private final AutoBatchConfig autoBatchConfig;
  private final boolean autoRunEnabled;
  private final Executor executor;
  private final List<CompletableFuture<Result<BatchReferenceResponse[]>>> futures;

  private final List<BatchReference> references;
  private String consistencyLevel;


  private ReferencesBatcher(CloseableHttpAsyncClient client, Config config, ReferencesPath referencesPath,
                            BatchRetriesConfig batchRetriesConfig, AutoBatchConfig autoBatchConfig,
                            Executor executor) {
    super(client, config);
    this.referencesPath = referencesPath;
    this.futures = Collections.synchronizedList(new ArrayList<>());
    this.references = Collections.synchronizedList(new ArrayList<>());
    this.batchRetriesConfig = batchRetriesConfig;
    this.executor = executor;

    if (autoBatchConfig != null) {
      this.autoRunEnabled = true;
      this.autoBatchConfig = autoBatchConfig;
    } else {
      this.autoRunEnabled = false;
      this.autoBatchConfig = null;
    }
  }

  public static ReferencesBatcher create(CloseableHttpAsyncClient client, Config config, ReferencesPath referencesPath,
                                         BatchRetriesConfig batchRetriesConfig, Executor executor) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    return new ReferencesBatcher(client, config, referencesPath, batchRetriesConfig, null, executor);
  }

  public static ReferencesBatcher createAuto(CloseableHttpAsyncClient client, Config config, ReferencesPath referencesPath,
                                             BatchRetriesConfig batchRetriesConfig, AutoBatchConfig autoBatchConfig,
                                             Executor executor) {
    Assert.requiredNotNull(batchRetriesConfig, "batchRetriesConfig");
    Assert.requiredNotNull(autoBatchConfig, "autoBatchConfig");
    return new ReferencesBatcher(client, config, referencesPath, batchRetriesConfig, autoBatchConfig, executor);
  }


  public ReferencesBatcher withReference(BatchReference reference) {
    return withReferences(reference);
  }

  public ReferencesBatcher withReferences(BatchReference... references) {
    this.references.addAll(Arrays.asList(references));
    autoRun();
    return this;
  }

  public ReferencesBatcher withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  @Override
  public Future<Result<BatchReferenceResponse[]>> run(FutureCallback<Result<BatchReferenceResponse[]>> callback) {
    CompletableFuture<Result<BatchReferenceResponse[]>> future = runAll();
    if (callback != null) {
      future = future.whenComplete((result, throwable) -> {
        if (throwable != null) {
          callback.failed((Exception) throwable);
        } else {
          callback.completed(result);
        }
      });
    }
    return future;
  }

  private CompletableFuture<Result<BatchReferenceResponse[]>> runAll() {
    if (!autoRunEnabled) {
      if (references.isEmpty()) {
        return CompletableFuture.completedFuture(new Result<>(0, new BatchReferenceResponse[0], null));
      }

      List<BatchReference> batch = extractBatch(references.size());
      return runBatchRecursively(batch, 0, 0);
    }

    if (!references.isEmpty()) {
      List<BatchReference> batch = extractBatch(references.size());
      runBatch(batch);
    }
    if (futures.isEmpty()) {
      return CompletableFuture.completedFuture(new Result<>(0, new BatchReferenceResponse[0], null));
    }

    CompletableFuture<?>[] futuresAsArray = futures.toArray(new CompletableFuture<?>[0]);
    return CompletableFuture.allOf(futuresAsArray).thenApply(v -> {
      List<BatchReferenceResponse> allResponses = new ArrayList<>();
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
      return new Result<>(lastErrStatusCode[0], allResponses.toArray(new BatchReferenceResponse[0]), errorResponse);
    });
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
      runBatch(batch);
    }
  }

  private void runBatch(List<BatchReference> batch) {
    CompletableFuture<Result<BatchReferenceResponse[]>> future = runBatchRecursively(batch, 0, 0);
    if (autoBatchConfig.callback != null) {
      future = future.whenComplete((result, e) -> autoBatchConfig.callback.accept(result));
    }
    futures.add(future);
  }

  private CompletableFuture<Result<BatchReferenceResponse[]>> runBatchRecursively(List<BatchReference> batch,
                                                                                  int connectionErrorCount, int timeoutErrorCount) {
    return Futures.handleAsync(internalRun(batch), (result, throwable) -> {
      if (throwable != null) {
        boolean executeAgain = false;
        int tempConnCount = connectionErrorCount;
        int tempTimeCount = timeoutErrorCount;
        int delay = 0;

        if (throwable instanceof ConnectException) {
          if (tempConnCount++ < batchRetriesConfig.maxConnectionRetries) {
            executeAgain = true;
            delay = tempConnCount * batchRetriesConfig.retriesIntervalMs;
          }
        } else if (throwable instanceof SocketTimeoutException) {
          if (tempTimeCount++ < batchRetriesConfig.maxTimeoutRetries) {
            executeAgain = true;
            delay = tempTimeCount * batchRetriesConfig.retriesIntervalMs;
          }
        }
        if (executeAgain) {
          int finalConnCount = tempConnCount;
          int finalTimeCount = tempTimeCount;
          try {
            return Futures.supplyDelayed(() -> runBatchRecursively(batch, finalConnCount, finalTimeCount), delay, executor);
          } catch (InterruptedException e) {
            throw new CompletionException(e);
          }
        }
      }

      return CompletableFuture.completedFuture(createFinalResultFromLastResult(result, throwable, batch));
    }, executor);
  }

  private CompletableFuture<Result<BatchReferenceResponse[]>> internalRun(List<BatchReference> batch) {
    CompletableFuture<Result<BatchReferenceResponse[]>> future = new CompletableFuture<>();
    BatchReference[] payload = batch.toArray(new BatchReference[0]);
    String path = referencesPath.buildCreate(ReferencesPath.Params.builder()
      .consistencyLevel(consistencyLevel)
      .build());

    sendPostRequest(path, payload, BatchReferenceResponse[].class, new FutureCallback<Result<BatchReferenceResponse[]>>() {
      @Override
      public void completed(Result<BatchReferenceResponse[]> batchResult) {
        future.complete(batchResult);
      }

      @Override
      public void failed(Exception e) {
        future.completeExceptionally(e);
      }

      @Override
      public void cancelled() {
      }
    });
    return future;
  }

  private Result<BatchReferenceResponse[]> createFinalResultFromLastResult(Result<BatchReferenceResponse[]> lastResult,
                                                                           Throwable throwable,
                                                                           List<BatchReference> batch) {
    if (lastResult != null) {
      return lastResult;
    }

    int statusCode = 0;
    String failedRefs = batch.stream()
      .map(ref -> ref.getFrom() + " => " + ref.getTo())
      .collect(Collectors.joining(", "));
    WeaviateErrorMessage failedRefsMessage = WeaviateErrorMessage.builder()
      .message("Failed refs: " + failedRefs)
      .build();
    WeaviateErrorMessage throwableMessage = WeaviateErrorMessage.builder()
      .message(throwable.getMessage())
      .throwable(throwable)
      .build();
    return new Result<>(statusCode, null, WeaviateErrorResponse.builder()
      .error(Arrays.asList(throwableMessage, failedRefsMessage))
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

    public static ReferencesBatcher.BatchRetriesConfig.BatchRetriesConfigBuilder defaultConfig() {
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

    int batchSize;
    Consumer<Result<BatchReferenceResponse[]>> callback;

    private AutoBatchConfig(int batchSize, Consumer<Result<BatchReferenceResponse[]>> callback) {
      Assert.requireGreaterEqual(batchSize, 1, "batchSize");

      this.batchSize = batchSize;
      this.callback = callback;
    }

    public static ReferencesBatcher.AutoBatchConfig.AutoBatchConfigBuilder defaultConfig() {
      return AutoBatchConfig.builder()
        .batchSize(BATCH_SIZE)
        .callback(null);
    }
  }
}
