package technology.semi.weaviate.client.v1.backup.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateMeta;

public class BackupGetter extends BaseClient<BackupCreateMeta[]> implements ClientResult<BackupCreateMeta[]> {

  private String storageName;

  public BackupGetter(Config config) {
    super(config);
  }

  public BackupGetter withStorageName(String storageName) {
    this.storageName = storageName;
    return this;
  }

  @Override
  public Result<BackupCreateMeta[]> run() {
    Response<BackupCreateMeta[]> response = this.sendGetRequest(path(), BackupCreateMeta[].class);
    return new Result<>(response);
  }

  private String path() {
    return String.format("/backups/%s", storageName);
  }
}
