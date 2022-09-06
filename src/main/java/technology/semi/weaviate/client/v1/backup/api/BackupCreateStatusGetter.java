package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateStatusResponse;

public class BackupCreateStatusGetter extends BaseClient<BackupCreateStatusResponse> implements ClientResult<BackupCreateStatusResponse> {

  private String backend;
  private String backupId;

  public BackupCreateStatusGetter(Config config) {
    super(config);
  }

  public BackupCreateStatusGetter withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupCreateStatusGetter withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  @Override
  public Result<BackupCreateStatusResponse> run() {
    return new Result<>(statusCreate());
  }

  Response<BackupCreateStatusResponse> statusCreate() {
    return sendGetRequest(path(), BackupCreateStatusResponse.class);
  }

  private String path() {
    return String.format("/backups/%s/%s", backend, backupId);
  }
}
