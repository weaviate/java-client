package io.weaviate.client.v1.backup.api;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;

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

    List<String> queryParams = Arrays.asList(
      UrlEncoder.encodeQueryParam("bucket", this.bucket),
      UrlEncoder.encodeQueryParam("path", this.path)
    );
    queryParams.removeIf(Objects::isNull);
    if (!queryParams.isEmpty()) {
      base = base + "?" + String.join("&", queryParams);
    }

    return base;
  }
}
