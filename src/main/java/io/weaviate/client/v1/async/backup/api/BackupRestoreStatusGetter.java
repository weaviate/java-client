package io.weaviate.client.v1.async.backup.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;

public class BackupRestoreStatusGetter extends AsyncBaseClient<BackupRestoreStatusResponse>
  implements AsyncClientResult<BackupRestoreStatusResponse> {

  private String backend;
  private String backupId;
  private String bucket;
  private String backupPath;

  public BackupRestoreStatusGetter(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
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
    this.backupPath = path;
    return this;
  }

  @Override
  public Future<Result<BackupRestoreStatusResponse>> run(FutureCallback<Result<BackupRestoreStatusResponse>> callback) {
    String path = String.format("/backups/%s/%s/restore", UrlEncoder.encodePathParam(backend), UrlEncoder.encodePathParam(backupId));

    List<String> queryParams = new ArrayList<>();
    if (this.bucket != null) {
      queryParams.add(UrlEncoder.encodeQueryParam("bucket", this.bucket));
    }
    if (this.backupPath != null) {
      queryParams.add(UrlEncoder.encodeQueryParam("path", this.backupPath));
    }

    if (!queryParams.isEmpty()) {
      path += "?" + String.join("&", queryParams);
    }
    return sendGetRequest(path, BackupRestoreStatusResponse.class, callback);
  }
}
