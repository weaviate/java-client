package io.weaviate.client.v1.backup.api;

import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;

import java.net.URISyntaxException;

import org.apache.hc.core5.net.URIBuilder;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class BackupRestoreStatusGetter extends BaseClient<BackupRestoreStatusResponse> implements ClientResult<BackupRestoreStatusResponse> {

  private String backend;
  private String backupId;
  private String bucket;
  private String path;

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

  public BackupRestoreStatusGetter withBucket(String bucket) {
    this.bucket = bucket;
    return this;
  }

  public BackupRestoreStatusGetter withPath(String path) {
    this.path = path;
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
    String base = String.format("/backups/%s/%s/restore", backend, backupId);
    try {
      return new URIBuilder(base)
      .addParameter("bucket", bucket)
      .addParameter("path", path)
      .toString();
    } catch (URISyntaxException e) {
      return base;
    }
  }
}
