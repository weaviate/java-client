package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateMeta;
import technology.semi.weaviate.client.v1.backup.model.CreateStatus;

public class BackupCreateHelper extends BaseClient<BackupCreateMeta> {

  private static final long WAIT_INTERVAL = 1000;

  public BackupCreateHelper(Config config) {
    super(config);
  }

  Result<BackupCreateMeta> create(String className, String storageName, String backupId) {
    return create(endpoint(className, storageName, backupId));
  }

  private Result<BackupCreateMeta> create(String endpoint) {
    Response<BackupCreateMeta> response = sendPostRequest(endpoint, new Object(), BackupCreateMeta.class);
    return new Result<>(response);
  }

  Result<BackupCreateMeta> statusCreate(String className, String storageName, String backupId) {
    return statusCreate(endpoint(className, storageName, backupId));
  }

  private Result<BackupCreateMeta> statusCreate(String endpoint) {
    Response<BackupCreateMeta> response = sendGetRequest(endpoint, BackupCreateMeta.class);
    return new Result<>(response);
  }

  Result<BackupCreateMeta> createAndWaitForCompletion(String className, String storageName, String backupId) {
    String endpoint = endpoint(className, storageName, backupId);
    Result<BackupCreateMeta> result = create(endpoint);
    if (result.hasErrors()) {
      return result;
    }

    while(true) {
      result = statusCreate(endpoint);
      if (result.hasErrors()) {
        return result;
      }

      switch (result.getResult().getStatus()) {
        case CreateStatus.SUCCESS:
        case CreateStatus.FAILED:
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
    return String.format("/schema/%s/snapshots/%s/%s", className, storageName, backupId);
  }
}
