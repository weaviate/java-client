package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateResponse;

public class BackupGetter extends BaseClient<BackupCreateResponse[]> implements ClientResult<BackupCreateResponse[]> {

  private String storageName;

  public BackupGetter(Config config) {
    super(config);
  }

  public BackupGetter withStorageName(String storageName) {
    this.storageName = storageName;
    return this;
  }

  @Override
  public Result<BackupCreateResponse[]> run() {
    Response<BackupCreateResponse[]> response = this.sendGetRequest(path(), BackupCreateResponse[].class);
    return new Result<>(response);
  }

  private String path() {
    return String.format("/backups/%s", storageName);
  }
}
