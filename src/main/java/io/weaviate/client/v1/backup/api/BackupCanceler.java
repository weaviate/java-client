package io.weaviate.client.v1.backup.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

/**
 * BackupCanceler can cancel an in-progress backup by ID.
 *
 * <p>
 * Canceling backups which have successfully completed before being interrupted is not supported and will result in an error.
 */
public class BackupCanceler extends BaseClient<Void> implements ClientResult<Void> {
  private String backend;
  private String backupId;

  public BackupCanceler(HttpClient client, Config config) {
    super(client, config);
  }

  public BackupCanceler withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupCanceler withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  @Override
  public Result<Void> run() {
    Response<Void> result = sendDeleteRequest(path(), null, Void.class);
    return new Result<>(result);
  }

  private String path() {
    return String.format("/backups/%s/%s", backend, backupId);
  }
}

