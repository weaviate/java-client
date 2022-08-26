package technology.semi.weaviate.client.v1.backup.api;

import lombok.RequiredArgsConstructor;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateMeta;

@RequiredArgsConstructor
public class BackupCreator implements ClientResult<BackupCreateMeta> {

  private final BackupCreateHelper helper;
  private String className;
  private String storageName;
  private String backupId;
  private boolean waitForCompletion;

  public BackupCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public BackupCreator withStorageName(String storageName) {
    this.storageName = storageName;
    return this;
  }

  public BackupCreator withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  public BackupCreator withWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }

  @Override
  public Result<BackupCreateMeta> run() {
    if (waitForCompletion) {
      return helper.createAndWaitForCompletion(className, storageName, backupId);
    }
    return helper.create(className, storageName, backupId);
  }
}
