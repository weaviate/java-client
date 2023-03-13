package io.weaviate.client.v1.backup;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.backup.api.BackupCreateStatusGetter;
import io.weaviate.client.v1.backup.api.BackupCreator;
import io.weaviate.client.v1.backup.api.BackupRestoreStatusGetter;
import io.weaviate.client.v1.backup.api.BackupRestorer;
import lombok.RequiredArgsConstructor;
import io.weaviate.client.Config;
//import api.backup.v1.io.weaviate.client.BackupGetter;


@RequiredArgsConstructor
public class Backup {

  private final HttpClient httpClient;
  private final Config config;

  public BackupCreator creator() {
    return new BackupCreator(httpClient, config, createStatusGetter());
  }

  public BackupCreateStatusGetter createStatusGetter() {
    return new BackupCreateStatusGetter(httpClient, config);
  }

  public BackupRestorer restorer() {
    return new BackupRestorer(httpClient, config, restoreStatusGetter());
  }

  public BackupRestoreStatusGetter restoreStatusGetter() {
    return new BackupRestoreStatusGetter(httpClient, config);
  }

//  public BackupGetter getter() {
//    return new BackupGetter(config);
//  }
}
