package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateMeta;
import technology.semi.weaviate.client.v1.backup.model.BackupCreatePayload;
import technology.semi.weaviate.client.v1.backup.model.CreateStatus;

public class BackupCreateHelper extends BaseClient<BackupCreateMeta> {

  private static final long WAIT_INTERVAL = 1000;

  public BackupCreateHelper(Config config) {
    super(config);
  }

  Result<BackupCreateMeta> create(String storageName, BackupCreatePayload payload) {
    Response<BackupCreateMeta> response = sendPostRequest(path(storageName), payload, BackupCreateMeta.class);
    return new Result<>(response);
  }

  Result<BackupCreateMeta> statusCreate(String storageName, String backupId) {
    Response<BackupCreateMeta> response = sendGetRequest(path(storageName, backupId), BackupCreateMeta.class);
    return new Result<>(response);
  }

  Result<BackupCreateMeta> createAndWaitForCompletion(String storageName, BackupCreatePayload payload) {
    Result<BackupCreateMeta> result = create(storageName, payload);
    if (result.hasErrors()) {
      return result;
    }

    while(true) {
      result = statusCreate(storageName, payload.getId());
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

  private String path(String storageName) {
    return String.format("/backups/%s", storageName);
  }

  private String path(String storageName, String backupId) {
    return String.format("/backups/%s/%s", storageName, backupId);
  }
}
