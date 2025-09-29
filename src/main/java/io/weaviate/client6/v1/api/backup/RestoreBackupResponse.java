package io.weaviate.client6.v1.api.backup;

import java.util.List;

public final class RestoreBackupResponse {
  private final Backup backup;

  RestoreBackupResponse(Backup backup) {
    this.backup = backup;
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
}
