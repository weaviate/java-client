package io.weaviate.client.v1.async.backup.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.concurrent.Future;

/**
 * BackupCanceler can cancel an in-progress backup by ID.
 *
 * <p>
 * Canceling backups which have successfully completed before being interrupted is not supported and will result in an error.
 */
public class BackupCanceler extends AsyncBaseClient<Void>
  implements AsyncClientResult<Void> {

  private String backend;
  private String backupId;
  private String bucket;
  private String path;


  public BackupCanceler(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
  }

  public BackupCanceler withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupCanceler withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  public BackupCanceler withBucket(String bucket) {
    this.bucket = bucket;
    return this;
  }

  public BackupCanceler withPath(String path) {
    this.path = path;
    return this;
  }


  @Override
  public Future<Result<Void>> run(FutureCallback<Result<Void>> callback) {
    String path = String.format("/backups/%s/%s", UrlEncoder.encodePathParam(backend), UrlEncoder.encodePathParam(backupId));
    try {
      path = new URIBuilder(path)
      .addParameter("bucket", bucket)
      .addParameter("path", this.path)
      .toString();
    } catch (URISyntaxException e) {
    }
    return sendDeleteRequest(path, null, Void.class, callback);
  }
}

