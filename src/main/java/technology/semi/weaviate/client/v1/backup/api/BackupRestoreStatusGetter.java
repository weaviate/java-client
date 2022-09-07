package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;

public class BackupRestoreStatusGetter extends BaseClient<BackupRestoreStatusResponse> implements ClientResult<BackupRestoreStatusResponse> {

  private String backend;
  private String backupId;

  public BackupRestoreStatusGetter(Config config) {
    super(config);
  }

  public BackupRestoreStatusGetter withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupRestoreStatusGetter withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  @Override
  public Result<BackupRestoreStatusResponse> run() {
    return new Result<>(statusRestore());
  }

  Response<BackupRestoreStatusResponse> statusRestore() {
    return sendGetRequest(path(), BackupRestoreStatusResponse.class);
  }

  private String path() {
    return String.format("/backups/%s/%s/restore", backend, backupId);
  }
}
