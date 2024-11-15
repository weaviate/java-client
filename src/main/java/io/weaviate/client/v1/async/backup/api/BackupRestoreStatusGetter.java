package io.weaviate.client.v1.async.backup.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.Future;

public class BackupRestoreStatusGetter extends AsyncBaseClient<BackupRestoreStatusResponse>
  implements AsyncClientResult<BackupRestoreStatusResponse> {

  private String backend;
  private String backupId;


  public BackupRestoreStatusGetter(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }


  public BackupRestoreStatusGetter withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupRestoreStatusGetter withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }


  @Override
  public Future<Result<BackupRestoreStatusResponse>> run(FutureCallback<Result<BackupRestoreStatusResponse>> callback) {
    String path = String.format("/backups/%s/%s/restore", UrlEncoder.encodePathParam(backend), UrlEncoder.encodePathParam(backupId));
    return sendGetRequest(path, BackupRestoreStatusResponse.class, callback);
  }
}
