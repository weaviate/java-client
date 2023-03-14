package io.weaviate.client.v1.backup.api;

import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class BackupRestoreStatusGetter extends BaseClient<BackupRestoreStatusResponse> implements ClientResult<BackupRestoreStatusResponse> {

  private String backend;
  private String backupId;

  public BackupRestoreStatusGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
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
