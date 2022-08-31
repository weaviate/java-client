package technology.semi.weaviate.client.v1.backup.api;

import lombok.RequiredArgsConstructor;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreMeta;
import technology.semi.weaviate.client.v1.backup.model.BackupRestorePayload;

@RequiredArgsConstructor
public class BackupRestorer implements ClientResult<BackupRestoreMeta> {

  private final BackupRestoreHelper helper;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String storageName;
  private String backupId;
  private boolean waitForCompletion;

  public BackupRestorer withIncludeClassNames(String... classNames) {
    this.includeClassNames = classNames;
    return this;
  }

  public BackupRestorer withExcludeClassNames(String... classNames) {
    this.excludeClassNames = classNames;
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
    BackupRestorePayload payload = BackupRestorePayload.builder()
      .id(backupId)
      .config(BackupRestorePayload.Config.builder().build())
      .include(includeClassNames)
      .exclude(excludeClassNames)
      .build();

    if (waitForCompletion) {
      return helper.restoreAndWaitForCompletion(storageName, payload);
    }
    return helper.restore(storageName, payload);
  }
}
