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

import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class Backup {

  private final CloseableHttpAsyncClient client;
  private final Config config;


  public BackupCreator creator() {
    return creator(null);
  }

  public BackupCreator creator(Executor executor) {
    return new BackupCreator(client, config, createStatusGetter(), executor);
  }

  public BackupCreateStatusGetter createStatusGetter() {
    return new BackupCreateStatusGetter(client, config);
  }

  public BackupRestorer restorer() {
    return restorer(null);
  }

  public BackupRestorer restorer(Executor executor) {
    return new BackupRestorer(client, config, restoreStatusGetter(), executor);
  }

  public BackupRestoreStatusGetter restoreStatusGetter() {
    return new BackupRestoreStatusGetter(client, config);
  }

  public BackupCanceler canceler() {
    return new BackupCanceler(client, config);
  }

  public BackupGetter getter() { // TODO: add test
    return new BackupGetter(client, config);
  }
}
