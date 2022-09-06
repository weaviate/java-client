package technology.semi.weaviate.client.v1.backup;

import lombok.RequiredArgsConstructor;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.backup.api.BackupCreateStatusGetter;
import technology.semi.weaviate.client.v1.backup.api.BackupCreator;
//import technology.semi.weaviate.client.v1.backup.api.BackupGetter;
import technology.semi.weaviate.client.v1.backup.api.BackupRestoreStatusGetter;
import technology.semi.weaviate.client.v1.backup.api.BackupRestorer;

@RequiredArgsConstructor
public class Backup {

  private final Config config;

  public BackupCreator creator() {
    return new BackupCreator(config, createStatusGetter());
  }

  public BackupCreateStatusGetter createStatusGetter() {
    return new BackupCreateStatusGetter(config);
  }

  public BackupRestorer restorer() {
    return new BackupRestorer(config, restoreStatusGetter());
  }

  public BackupRestoreStatusGetter restoreStatusGetter() {
    return new BackupRestoreStatusGetter(config);
  }

//  public BackupGetter getter() {
//    return new BackupGetter(config);
//  }
}
