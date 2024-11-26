package io.weaviate.client.v1.async.backup.api;

import com.google.gson.annotations.SerializedName;
import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.util.Futures;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.v1.backup.model.CreateStatus;
import lombok.Builder;
import lombok.Getter;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class BackupCreator extends AsyncBaseClient<BackupCreateResponse>
  implements AsyncClientResult<BackupCreateResponse> {

  private static final long WAIT_INTERVAL = 1000;

  private final BackupCreateStatusGetter statusGetter;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String backend;
  private String backupId;
  private BackupCreateConfig config;
  private boolean waitForCompletion;
  private final Executor executor;


  public BackupCreator(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider, BackupCreateStatusGetter statusGetter, Executor executor) {
    super(client, config, tokenProvider);
    this.statusGetter = statusGetter;
    this.executor = executor;
  }


  public BackupCreator withIncludeClassNames(String... classNames) {
    this.includeClassNames = classNames;
    return this;
  }

  public BackupCreator withExcludeClassNames(String... classNames) {
    this.excludeClassNames = classNames;
    return this;
  }

  public BackupCreator withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupCreator withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  public BackupCreator withConfig(BackupCreateConfig config) {
    this.config = config;
    return this;
  }

  public BackupCreator withWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }


  @Override
  public Future<Result<BackupCreateResponse>> run(FutureCallback<Result<BackupCreateResponse>> callback) {
    if (waitForCompletion) {
      return createAndWaitForCompletion(callback);
    }
    return create(callback);
  }

  private Future<Result<BackupCreateResponse>> create(FutureCallback<Result<BackupCreateResponse>> callback) {
    BackupCreate payload = BackupCreate.builder()
      .id(backupId)
      .config(config)
      .include(includeClassNames)
      .exclude(excludeClassNames)
      .build();
    String path = String.format("/backups/%s", UrlEncoder.encodePathParam(backend));
    return sendPostRequest(path, payload, BackupCreateResponse.class, callback);
  }

  private Future<Result<BackupCreateResponse>> createAndWaitForCompletion(FutureCallback<Result<BackupCreateResponse>> callback) {
    CompletableFuture<Result<BackupCreateResponse>> future = new CompletableFuture<>();
    FutureCallback<Result<BackupCreateResponse>> internalCallback = new FutureCallback<Result<BackupCreateResponse>>() {
      @Override
      public void completed(Result<BackupCreateResponse> backupCreateResult) {
        future.complete(backupCreateResult);
      }

      @Override
      public void failed(Exception e) {
        future.completeExceptionally(e);
      }

      @Override
      public void cancelled() {
        future.cancel(true);
        if (callback != null) {
          callback.cancelled(); // TODO:AL propagate cancel() call from future to completable future
        }
      }
    };

    create(internalCallback);

    return future.thenCompose(createResult -> {
        if (createResult.hasErrors()) {
          return CompletableFuture.completedFuture(createResult);
        }
        return getStatusRecursively(backend, backupId, createResult);
      })
      .whenComplete((createResult, throwable) -> {
        if (callback != null) {
          if (throwable != null) {
            callback.failed((Exception) throwable);
          } else {
            callback.completed(createResult);
          }
        }
      });
  }

  private CompletableFuture<Result<BackupCreateStatusResponse>> getStatus(String backend, String backupId) {
    CompletableFuture<Result<BackupCreateStatusResponse>> future = new CompletableFuture<>();
    statusGetter.withBackend(backend).withBackupId(backupId)
      .run(new FutureCallback<Result<BackupCreateStatusResponse>>() {
        @Override
        public void completed(Result<BackupCreateStatusResponse> createStatusResult) {
          future.complete(createStatusResult);
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

  private CompletableFuture<Result<BackupCreateResponse>> getStatusRecursively(String backend, String backupId,
                                                                               Result<BackupCreateResponse> createResult) {
    return Futures.thenComposeAsync(getStatus(backend, backupId), createStatusResult -> {
      boolean isRunning = Optional.of(createStatusResult)
        .filter(r -> !r.hasErrors())
        .map(Result::getResult)
        .map(BackupCreateStatusResponse::getStatus)
        .filter(status -> {
          switch (status) {
            case CreateStatus.SUCCESS:
            case CreateStatus.FAILED:
              return false;
            default:
              return true;
          }
        })
        .isPresent();

      if (isRunning) {
        try {
          return Futures.supplyDelayed(() -> getStatusRecursively(backend, backupId, createResult), WAIT_INTERVAL, executor);
        } catch (InterruptedException e) {
          throw new CompletionException(e);
        }
      }
      return CompletableFuture.completedFuture(merge(createStatusResult, createResult));
    }, executor);
  }

  private Result<BackupCreateResponse> merge(Result<BackupCreateStatusResponse> createStatusResult,
                                             Result<BackupCreateResponse> createResult) {
    BackupCreateStatusResponse createStatusResponse = createStatusResult.getResult();
    BackupCreateResponse createResponse = createResult.getResult();

    BackupCreateResponse merged = null;
    int statusCode = HttpStatus.SC_OK;
    WeaviateErrorResponse errorResponse = null;

    if (createStatusResponse != null) {
      merged = new BackupCreateResponse();

      merged.setId(createStatusResponse.getId());
      merged.setBackend(createStatusResponse.getBackend());
      merged.setPath(createStatusResponse.getPath());
      merged.setStatus(createStatusResponse.getStatus());
      merged.setError(createStatusResponse.getError());
      merged.setClassNames(createResponse.getClassNames());
    }
    if (createStatusResult.hasErrors()) {
      WeaviateError error = createStatusResult.getError();
      statusCode = error.getStatusCode();
      List<WeaviateErrorMessage> messages = error.getMessages();

      errorResponse = WeaviateErrorResponse.builder()
        .code(statusCode)
        .error(messages)
        .build();
    }

    return new Result<>(statusCode, merged, errorResponse);
  }


  @Getter
  @Builder
  private static class BackupCreate {
    String id;
    String[] include;
    String[] exclude;
    BackupCreateConfig config;
  }

  @Getter
  @Builder
  public static class BackupCreateConfig {
    @SerializedName("CPUPercentage")
    Integer cpuPercentage;
    @SerializedName("ChunkSize")
    Integer chunkSize;
    @SerializedName("CompressionLevel")
    String compressionLevel;
  }

  public interface BackupCompression {
    String DEFAULT_COMPRESSION = "DefaultCompression";
    String BEST_SPEED = "BestSpeed";
    String BEST_COMPRESSION = "BestCompression";
  }
}
