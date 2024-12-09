package io.weaviate.client.v1.async.backup.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.concurrent.Future;

public class BackupRestoreStatusGetter extends AsyncBaseClient<BackupRestoreStatusResponse>
  implements AsyncClientResult<BackupRestoreStatusResponse> {

  private String backend;
  private String backupId;
  private String bucket;
  private String path;

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
    this.path = path;
    return this;
  }

  @Override
  public Future<Result<BackupRestoreStatusResponse>> run(FutureCallback<Result<BackupRestoreStatusResponse>> callback) {
    String path = String.format("/backups/%s/%s/restore", UrlEncoder.encodePathParam(backend), UrlEncoder.encodePathParam(backupId));
     try {
      path = new URIBuilder(path)
      .addParameter("bucket", bucket)
      .addParameter("path", this.path)
      .toString();
    } catch (URISyntaxException e) {
    }
    return sendGetRequest(path, BackupRestoreStatusResponse.class, callback);
  }
}
