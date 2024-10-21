package io.weaviate.client.v1.backup;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.backup.api.*;
import lombok.RequiredArgsConstructor;

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

  public BackupCanceler canceler() {
    return new BackupCanceler(httpClient, config);
  }
}
