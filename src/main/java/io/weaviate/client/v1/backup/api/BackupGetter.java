package io.weaviate.client.v1.backup.api;

import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class BackupGetter extends BaseClient<BackupCreateResponse[]> implements ClientResult<BackupCreateResponse[]> {

  private String backend;

  public BackupGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public BackupGetter withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  @Override
  public Result<BackupCreateResponse[]> run() {
    Response<BackupCreateResponse[]> response = this.sendGetRequest(path(), BackupCreateResponse[].class);
    return new Result<>(response);
  }

  private String path() {
    return String.format("/backups/%s", backend);
  }
}
