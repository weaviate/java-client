package technology.semi.weaviate.client.v1.backup.api;

import lombok.RequiredArgsConstructor;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateMeta;

@RequiredArgsConstructor
public class BackupCreateStatusGetter implements ClientResult<BackupCreateMeta> {

  private final BackupCreateHelper helper;
  private String storageName;
  private String backupId;

  public BackupCreateStatusGetter withStorageName(String storageName) {
    this.storageName = storageName;
    return this;
  }

  public BackupCreateStatusGetter withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  @Override
  public Result<BackupCreateMeta> run() {
    return helper.statusCreate(storageName, backupId);
  }
}
