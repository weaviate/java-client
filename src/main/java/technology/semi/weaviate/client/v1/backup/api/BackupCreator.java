package technology.semi.weaviate.client.v1.backup.api;

import lombok.RequiredArgsConstructor;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateMeta;
import technology.semi.weaviate.client.v1.backup.model.BackupCreatePayload;

@RequiredArgsConstructor
public class BackupCreator implements ClientResult<BackupCreateMeta> {

  private final BackupCreateHelper helper;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String storageName;
  private String backupId;
  private boolean waitForCompletion;


  public BackupCreator withIncludeClassNames(String... classNames) {
    this.includeClassNames = classNames;
    return this;
  }

  public BackupCreator withExcludeClassNames(String... classNames) {
    this.excludeClassNames = classNames;
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
    BackupCreatePayload payload = BackupCreatePayload.builder()
      .id(backupId)
      .config(BackupCreatePayload.Config.builder().build())
      .include(includeClassNames)
      .exclude(excludeClassNames)
      .build();

    if (waitForCompletion) {
      return helper.createAndWaitForCompletion(storageName, payload);
    }
    return helper.create(storageName, payload);
  }
}
