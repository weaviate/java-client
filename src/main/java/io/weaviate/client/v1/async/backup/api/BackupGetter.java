package io.weaviate.client.v1.async.backup.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.Future;

public class BackupGetter extends AsyncBaseClient<BackupCreateResponse[]>
  implements AsyncClientResult<BackupCreateResponse[]> {

  private String backend;


  public BackupGetter(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }


  public BackupGetter withBackend(String backend) {
    this.backend = backend;
    return this;
  }


  @Override
  public Future<Result<BackupCreateResponse[]>> run(FutureCallback<Result<BackupCreateResponse[]>> callback) {
    String path = String.format("/backups/%s", UrlEncoder.encodePathParam(backend));
    return sendGetRequest(path, BackupCreateResponse[].class, callback);
  }
}
