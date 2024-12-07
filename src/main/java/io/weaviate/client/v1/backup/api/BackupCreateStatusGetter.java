package io.weaviate.client.v1.backup.api;

import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;

import java.net.URISyntaxException;

import org.apache.hc.core5.net.URIBuilder;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class BackupCreateStatusGetter extends BaseClient<BackupCreateStatusResponse> implements ClientResult<BackupCreateStatusResponse> {

  private String backend;
  private String backupId;
  private String bucket;
  private String path;

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

  public BackupCreateStatusGetter withBucket(String bucket) {
    this.bucket = bucket;
    return this;
  }

  public BackupCreateStatusGetter withPath(String path) {
    this.path = path;
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
    String base = String.format("/backups/%s/%s", backend, backupId);
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
