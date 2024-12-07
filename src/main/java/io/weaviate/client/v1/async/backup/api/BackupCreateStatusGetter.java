package io.weaviate.client.v1.async.backup.api;

import java.net.URISyntaxException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.URIBuilder;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;

public class BackupCreateStatusGetter extends AsyncBaseClient<BackupCreateStatusResponse>
  implements AsyncClientResult<BackupCreateStatusResponse> {

  private String backend;
  private String backupId;
  private String bucket;
  private String path;

  public BackupCreateStatusGetter(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
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
  public Future<Result<BackupCreateStatusResponse>> run(FutureCallback<Result<BackupCreateStatusResponse>> callback) {
    String path = String.format("/backups/%s/%s", UrlEncoder.encodePathParam(backend), UrlEncoder.encodePathParam(backupId));
    try {
      path =  new URIBuilder(path)
      .addParameter("bucket", bucket)
      .addParameter("path", this.path)
      .toString();
    } catch (URISyntaxException e) {
    }
    return sendGetRequest(path, BackupCreateStatusResponse.class, callback);
  }
}
