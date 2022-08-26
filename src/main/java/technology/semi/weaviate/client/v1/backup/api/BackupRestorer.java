package technology.semi.weaviate.client.v1.backup.api;

import lombok.RequiredArgsConstructor;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreMeta;

@RequiredArgsConstructor
public class BackupRestorer implements ClientResult<BackupRestoreMeta> {

  private final BackupRestoreHelper helper;
  private String className;
  private String storageName;
  private String backupId;
  private boolean waitForCompletion;

  public BackupRestorer withClassName(String className) {
    this.className = className;
    return this;
  }

  public BackupRestorer withStorageName(String storageName) {
    this.storageName = storageName;
    return this;
  }

  public BackupRestorer withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  public BackupRestorer withWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }

  @Override
  public Result<BackupRestoreMeta> run() {
    if (waitForCompletion) {
      return helper.restoreAndWaitForCompletion(className, storageName, backupId);
    }
    return helper.restore(className, storageName, backupId);
  }
}
