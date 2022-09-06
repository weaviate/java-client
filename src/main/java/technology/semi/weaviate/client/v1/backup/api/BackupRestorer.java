package technology.semi.weaviate.client.v1.backup.api;

import lombok.Builder;
import lombok.Getter;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreResponse;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import technology.semi.weaviate.client.v1.backup.model.RestoreStatus;

public class BackupRestorer extends BaseClient<BackupRestoreResponse> implements ClientResult<BackupRestoreResponse> {

  private static final long WAIT_INTERVAL = 1000;

  private final BackupRestoreStatusGetter statusGetter;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String storageName;
  private String backupId;
  private boolean waitForCompletion;

  public BackupRestorer(Config config, BackupRestoreStatusGetter statusGetter) {
    super(config);
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

  public BackupRestorer withStorageName(String storageName) {
    this.storageName = storageName;
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
  public Result<BackupRestoreResponse> run() {
    BackupRestore payload = BackupRestore.builder()
      .config(BackupRestoreConfig.builder().build())
      .include(includeClassNames)
      .exclude(excludeClassNames)
      .build();

    if (waitForCompletion) {
      return restoreAndWaitForCompletion(payload);
    }
    return restore(payload);
  }


  private Result<BackupRestoreResponse> restore(BackupRestore payload) {
    Response<BackupRestoreResponse> response = sendPostRequest(path(storageName, backupId), payload, BackupRestoreResponse.class);
    return new Result<>(response);
  }

  private Result<BackupRestoreResponse> restoreAndWaitForCompletion(BackupRestore payload) {
    Result<BackupRestoreResponse> result = restore(payload);
    if (result.hasErrors()) {
      return result;
    }

    statusGetter.withStorageName(storageName).withBackupId(backupId);
    while(true) {
      Response<BackupRestoreStatusResponse> statusResponse = statusGetter.statusRestore();
      if (new Result<>(statusResponse).hasErrors()) {
        return merge(statusResponse, result);
      }

      switch (statusResponse.getBody().getStatus()) {
        case RestoreStatus.SUCCESS:
        case RestoreStatus.FAILED:
          return merge(statusResponse, result);
      }

      try {
        Thread.sleep(WAIT_INTERVAL);
      } catch (InterruptedException e) {
        return merge(statusResponse, result);
      }
    }
  }

  private String path(String storageName, String backupId) {
    return String.format("/backups/%s/%s/restore", storageName, backupId);
  }

  private Result<BackupRestoreResponse> merge(Response<BackupRestoreStatusResponse> response, Result<BackupRestoreResponse> result) {
    BackupRestoreStatusResponse statusRestoreResponse = response.getBody();
    BackupRestoreResponse restoreResponse = result.getResult();

    BackupRestoreResponse merged = null;
    if (statusRestoreResponse != null) {
      merged = new BackupRestoreResponse();

      merged.setId(statusRestoreResponse.getId());
      merged.setStorageName(statusRestoreResponse.getStorageName());
      merged.setPath(statusRestoreResponse.getPath());
      merged.setStatus(statusRestoreResponse.getStatus());
      merged.setError(statusRestoreResponse.getError());
      merged.setClassNames(restoreResponse.getClassNames());
    }

    return new Result<>(response.getStatusCode(), merged, response.getErrors());
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
  private static class BackupRestoreConfig {
    // TBD
  }
}
