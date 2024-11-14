package io.weaviate.client.v1.async.backup.api;

import com.google.gson.annotations.SerializedName;
import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.backup.model.BackupRestoreResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.v1.backup.model.RestoreStatus;
import lombok.Builder;
import lombok.Getter;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;

public class BackupRestorer extends AsyncBaseClient<BackupRestoreResponse>
  implements AsyncClientResult<BackupRestoreResponse> {

  private static final long WAIT_INTERVAL = 1000;

  private final BackupRestoreStatusGetter statusGetter;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String backend;
  private String backupId;
  private BackupRestoreConfig config;
  private boolean waitForCompletion;


  public BackupRestorer(CloseableHttpAsyncClient client, Config config, BackupRestoreStatusGetter statusGetter) {
    super(client, config);
    this.statusGetter = statusGetter;
  }


  public BackupRestorer withIncludeClassNames(String... classNames) {
    this.includeClassNames = classNames;
    return this;
  }

  public BackupRestorer withExcludeClassNames(String... classNames) {
    this.excludeClassNames = classNames;
    return this;
  }

  public BackupRestorer withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupRestorer withConfig(BackupRestoreConfig config) {
    this.config = config;
    return this;
  }

  public BackupRestorer withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  public BackupRestorer withWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }

  @Override
  public Future<Result<BackupRestoreResponse>> run(FutureCallback<Result<BackupRestoreResponse>> callback) {
    if (waitForCompletion) {
      return restoreAndWaitForCompletion(callback);
    }
    return restore(callback);
  }


  private Future<Result<BackupRestoreResponse>> restore(FutureCallback<Result<BackupRestoreResponse>> callback) {
    BackupRestore payload = BackupRestore.builder()
      .config(BackupRestoreConfig.builder().build())
      .include(includeClassNames)
      .exclude(excludeClassNames)
      .config(config)
      .build();
    String path = String.format("/backups/%s/%s/restore", UrlEncoder.encodePathParam(backend), UrlEncoder.encodePathParam(backupId));
    return sendPostRequest(path, payload, BackupRestoreResponse.class, callback);
  }

  private Future<Result<BackupRestoreResponse>> restoreAndWaitForCompletion(FutureCallback<Result<BackupRestoreResponse>> callback) {
    CompletableFuture<Result<BackupRestoreResponse>> future = new CompletableFuture<>();
    FutureCallback<Result<BackupRestoreResponse>> internalCallback = new FutureCallback<Result<BackupRestoreResponse>>() {
      @Override
      public void completed(Result<BackupRestoreResponse> backupRestoreResult) {
        future.complete(backupRestoreResult);
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

    restore(internalCallback);

    return future.thenCompose(restoreResult -> {
        if (restoreResult.hasErrors()) {
          return CompletableFuture.completedFuture(restoreResult);
        }
        return getStatusRecursively(backend, backupId, restoreResult);
      })
      .whenComplete((restoreResult, throwable) -> {
        if (callback != null) {
          if (throwable != null) {
            callback.failed((Exception) throwable);
          } else {
            callback.completed(restoreResult);
          }
        }
      });
  }

  private CompletableFuture<Result<BackupRestoreStatusResponse>> getStatus(String backend, String backupId) {
    CompletableFuture<Result<BackupRestoreStatusResponse>> future = new CompletableFuture<>();
    statusGetter.withBackend(backend).withBackupId(backupId)
      .run(new FutureCallback<Result<BackupRestoreStatusResponse>>() {
        @Override
        public void completed(Result<BackupRestoreStatusResponse> createStatusResult) {
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

  private CompletableFuture<Result<BackupRestoreResponse>> getStatusRecursively(String backend, String backupId,
                                                                                Result<BackupRestoreResponse> restoreResult) {
    return getStatus(backend, backupId).thenCompose(restoreStatusResult -> {
      boolean isRunning = Optional.of(restoreStatusResult)
        .filter(r -> !r.hasErrors())
        .map(Result::getResult)
        .map(BackupRestoreStatusResponse::getStatus)
        .filter(status -> {
          switch (status) {
            case RestoreStatus.SUCCESS:
            case RestoreStatus.FAILED:
              return false;
            default:
              return true;
          }
        })
        .isPresent();

      if (isRunning) {
        try {
          Thread.sleep(WAIT_INTERVAL);
          return getStatusRecursively(backend, backupId, restoreResult);
        } catch (InterruptedException e) {
          throw new CompletionException(e);
        }
      }
      return CompletableFuture.completedFuture(merge(restoreStatusResult, restoreResult));
    });
  }

  private Result<BackupRestoreResponse> merge(Result<BackupRestoreStatusResponse> restoreStatusResult,
                                              Result<BackupRestoreResponse> restoreResult) {
    BackupRestoreStatusResponse restoreStatusResponse = restoreStatusResult.getResult();
    BackupRestoreResponse restoreResponse = restoreResult.getResult();

    BackupRestoreResponse merged = null;
    int statusCode = HttpStatus.SC_OK;
    WeaviateErrorResponse errorResponse = null;

    if (restoreStatusResponse != null) {
      merged = new BackupRestoreResponse();

      merged.setId(restoreStatusResponse.getId());
      merged.setBackend(restoreStatusResponse.getBackend());
      merged.setPath(restoreStatusResponse.getPath());
      merged.setStatus(restoreStatusResponse.getStatus());
      merged.setError(restoreStatusResponse.getError());
      merged.setClassNames(restoreResponse.getClassNames());
    }
    if (restoreStatusResult.hasErrors()) {
      WeaviateError error = restoreStatusResult.getError();
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
  private static class BackupRestore {
    BackupRestoreConfig config;
    String[] include;
    String[] exclude;
  }

  @Getter
  @Builder
  public static class BackupRestoreConfig {
    @SerializedName("CPUPercentage")
    Integer cpuPercentage;
  }
}
