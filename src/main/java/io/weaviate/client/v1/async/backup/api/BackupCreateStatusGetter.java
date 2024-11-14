package io.weaviate.client.v1.async.backup.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.Future;

public class BackupCreateStatusGetter extends AsyncBaseClient<BackupCreateStatusResponse>
  implements AsyncClientResult<BackupCreateStatusResponse> {

  private String backend;
  private String backupId;


  public BackupCreateStatusGetter(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }


  public BackupCreateStatusGetter withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupCreateStatusGetter withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  @Override
  public Future<Result<BackupCreateStatusResponse>> run(FutureCallback<Result<BackupCreateStatusResponse>> callback) {
    String path = String.format("/backups/%s/%s", UrlEncoder.encodePathParam(backend), UrlEncoder.encodePathParam(backupId));
    return sendGetRequest(path, BackupCreateStatusResponse.class, callback);
  }
}
