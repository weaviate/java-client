package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreMeta;
import technology.semi.weaviate.client.v1.backup.model.RestoreStatus;

public class BackupRestoreHelper extends BaseClient<BackupRestoreMeta> {

  private static final long WAIT_INTERVAL = 1000;

  public BackupRestoreHelper(Config config) {
    super(config);
  }

  Result<BackupRestoreMeta> restore(String className, String storageName, String backupId) {
    return restore(endpoint(className, storageName, backupId));
  }

  private Result<BackupRestoreMeta> restore(String endpoint) {
    Response<BackupRestoreMeta> response = sendPostRequest(endpoint, new Object(), BackupRestoreMeta.class);
    return new Result<>(response);
  }

  Result<BackupRestoreMeta> statusRestore(String className, String storageName, String backupId) {
    return statusRestore(endpoint(className, storageName, backupId));
  }

  private Result<BackupRestoreMeta> statusRestore(String endpoint) {
    Response<BackupRestoreMeta> response = sendGetRequest(endpoint, BackupRestoreMeta.class);
    return new Result<>(response);
  }

  Result<BackupRestoreMeta> restoreAndWaitForCompletion(String className, String storageName, String backupId) {
    String endpoint = endpoint(className, storageName, backupId);
    Result<BackupRestoreMeta> result = restore(endpoint);
    if (result.hasErrors()) {
      return result;
    }

    while(true) {
      result = statusRestore(endpoint);
      if (result.hasErrors()) {
        return result;
      }

      switch (result.getResult().getStatus()) {
        case RestoreStatus.SUCCESS:
        case RestoreStatus.FAILED:
          return result;
      }

      try {
        Thread.sleep(WAIT_INTERVAL);
      } catch (InterruptedException e) {
        return result;
      }
    }
  }

  private String endpoint(String className, String storageName, String backupId) {
    // TODO change snapshots to backups
    return String.format("/schema/%s/snapshots/%s/%s/restore", className, storageName, backupId);
  }
}
