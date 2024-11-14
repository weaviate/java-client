package io.weaviate.client.v1.async.backup;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.backup.api.BackupCanceler;
import io.weaviate.client.v1.async.backup.api.BackupCreateStatusGetter;
import io.weaviate.client.v1.async.backup.api.BackupCreator;
import io.weaviate.client.v1.async.backup.api.BackupGetter;
import io.weaviate.client.v1.async.backup.api.BackupRestoreStatusGetter;
import io.weaviate.client.v1.async.backup.api.BackupRestorer;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

@RequiredArgsConstructor
public class Backup {

  private final CloseableHttpAsyncClient client;
  private final Config config;


  public BackupCreator creator() {
    return new BackupCreator(client, config, createStatusGetter());
  }

  public BackupCreateStatusGetter createStatusGetter() {
    return new BackupCreateStatusGetter(client, config);
  }

  public BackupRestorer restorer() {
    return new BackupRestorer(client, config, restoreStatusGetter());
  }

  public BackupRestoreStatusGetter restoreStatusGetter() {
    return new BackupRestoreStatusGetter(client, config);
  }

  public BackupCanceler canceler() {
    return new BackupCanceler(client, config);
  }

  public BackupGetter getter() {
    return new BackupGetter(client, config);
  }
}
