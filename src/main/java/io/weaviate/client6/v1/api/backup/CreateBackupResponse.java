package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public final class CreateBackupResponse {
  private final Backup backup;
  private final WeaviateBackupClient backupClient;

  CreateBackupResponse(final Backup backup, final WeaviateBackupClient backupClient) {
    this.backup = backup;
    this.backupClient = backupClient;
  }

  public Backup backup() {
    return backup;
  }

  public String id() {
    return backup.id();
  }

  public String path() {
    return backup.path();
  }

  public String backend() {
    return backup.backend();
  }

  public List<String> includesCollections() {
    return backup.includesCollections();
  }

  public BackupStatus status() {
    return backup.status();
  }

  public String error() {
    return backup.error();
  }

  public Backup waitForCompletion() throws IOException, TimeoutException {
    return waitForStatus(BackupStatus.SUCCESS);
  }

  public Backup waitForStatus(BackupStatus status) throws IOException, TimeoutException {
    return new Waiter(backup, this::poll).waitForStatus(status);
  }

  private Optional<Backup> poll() throws Exception {
    return this.backupClient.getCreateStatus(id(), backend());
  }

  public void cancel() throws IOException {
    this.backupClient.cancel(id(), backend());
  }
}
