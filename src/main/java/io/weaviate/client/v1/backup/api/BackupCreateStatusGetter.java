package io.weaviate.client.v1.backup.api;

import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class BackupCreateStatusGetter extends BaseClient<BackupCreateStatusResponse> implements ClientResult<BackupCreateStatusResponse> {

  private String backend;
  private String backupId;

  public BackupCreateStatusGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
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
