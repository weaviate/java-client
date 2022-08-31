package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreMeta;
import technology.semi.weaviate.client.v1.backup.model.BackupRestorePayload;
import technology.semi.weaviate.client.v1.backup.model.RestoreStatus;

public class BackupRestoreHelper extends BaseClient<BackupRestoreMeta> {

  private static final long WAIT_INTERVAL = 1000;

  public BackupRestoreHelper(Config config) {
    super(config);
  }

  Result<BackupRestoreMeta> restore(String storageName, BackupRestorePayload payload) {
    Response<BackupRestoreMeta> response = sendPostRequest(path(storageName, payload.getId()), payload, BackupRestoreMeta.class);
    return new Result<>(response);
  }

  Result<BackupRestoreMeta> statusRestore(String storageName, String backupId) {
    Response<BackupRestoreMeta> response = sendGetRequest(path(storageName, backupId), BackupRestoreMeta.class);
    return new Result<>(response);
  }

  Result<BackupRestoreMeta> restoreAndWaitForCompletion(String storageName, BackupRestorePayload payload) {
    Result<BackupRestoreMeta> result = restore(storageName, payload);
    if (result.hasErrors()) {
      return result;
    }

    while(true) {
      result = statusRestore(storageName, payload.getId());
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

  private String path(String storageName, String backupId) {
    return String.format("/backups/%s/%s/restore", storageName, backupId);
  }
}
