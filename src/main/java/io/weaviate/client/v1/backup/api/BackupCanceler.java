package io.weaviate.client.v1.backup.api;

import java.net.URISyntaxException;

import org.apache.hc.core5.net.URIBuilder;

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
  private String bucket;
  private String path;

  public BackupCanceler(HttpClient client, Config config) {
    super(client, config);
  }

  public BackupCanceler withBackend(String backend) {
    this.backend = backend;
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

