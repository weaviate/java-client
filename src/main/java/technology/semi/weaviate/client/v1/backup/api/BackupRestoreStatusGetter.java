package technology.semi.weaviate.client.v1.backup.api;

import lombok.RequiredArgsConstructor;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreMeta;

@RequiredArgsConstructor
public class BackupRestoreStatusGetter implements ClientResult<BackupRestoreMeta> {

  private final BackupRestoreHelper helper;
  private String storageName;
  private String backupId;

  public BackupRestoreStatusGetter withStorageName(String storageName) {
    this.storageName = storageName;
    return this;
  }

  public BackupRestoreStatusGetter withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  @Override
  public Result<BackupRestoreMeta> run() {
    return helper.statusRestore(storageName, backupId);
  }
}
