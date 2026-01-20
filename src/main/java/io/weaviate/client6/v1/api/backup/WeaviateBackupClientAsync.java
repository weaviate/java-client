package io.weaviate.client6.v1.api.backup;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateBackupClientAsync {
  private final RestTransport restTransport;

  public WeaviateBackupClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Start a new backup process.
   *
   * @param backupId Backup ID. Must be unique for the backend.
   * @param backend  Backup storage backend.
   */
  public CompletableFuture<Backup> create(String backupId, String backend) {
    return create(new CreateBackupRequest(CreateBackupRequest.BackupCreate.of(backupId), backend));
  }

  /**
   * Start a new backup process.
   *
   * @param backupId Backup ID. Must be unique for the backend.
   * @param backend  Backup storage backend.
   * @param fn       Lambda expression for optional parameters.
   */
  public CompletableFuture<Backup> create(String backupId, String backend,
      Function<CreateBackupRequest.BackupCreate.Builder, ObjectBuilder<CreateBackupRequest.BackupCreate>> fn) {
    return create(new CreateBackupRequest(CreateBackupRequest.BackupCreate.of(backupId, fn), backend));
  }

  /**
   * Start a new backup process.
   *
   * @param request Create backup request.
   */
  public CompletableFuture<Backup> create(CreateBackupRequest request) {
    return this.restTransport.performRequestAsync(request, CreateBackupRequest._ENDPOINT)
        .thenApply(bak -> bak.withOperation(Backup.Operation.CREATE));
  }

  /**
   * Get backup create status.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   */
  public CompletableFuture<Optional<Backup>> getCreateStatus(String backupId, String backend) {
    return this.restTransport.performRequestAsync(
        new GetCreateStatusRequest(backupId, backend), GetCreateStatusRequest._ENDPOINT)
        .thenApply(bak -> bak.map(_bak -> _bak.withOperation(Backup.Operation.CREATE)));
  }

  /**
   * Start backup restore process.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   */
  public CompletableFuture<Backup> restore(String backupId, String backend) {
    return restore(new RestoreBackupRequest(backupId, backend, RestoreBackupRequest.BackupRestore.of()));
  }

  /**
   * Start backup restore process.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   * @param fn       Lambda expression for optional parameters.
   */
  public CompletableFuture<Backup> restore(String backupId, String backend,
      Function<RestoreBackupRequest.BackupRestore.Builder, ObjectBuilder<RestoreBackupRequest.BackupRestore>> fn) {
    return restore(new RestoreBackupRequest(backupId, backend, RestoreBackupRequest.BackupRestore.of(fn)));
  }

  /**
   * Start backup restore process.
   *
   * @param request Restore backup request.
   */
  public CompletableFuture<Backup> restore(RestoreBackupRequest request) {
    return this.restTransport.performRequestAsync(request, RestoreBackupRequest._ENDPOINT)
        .thenApply(bak -> bak.withOperation(Backup.Operation.RESTORE));
  }

  /**
   * Get backup restore status.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   */
  public CompletableFuture<Optional<Backup>> getRestoreStatus(String backupId, String backend) {
    return this.restTransport
        .performRequestAsync(new GetRestoreStatusRequest(backupId, backend), GetRestoreStatusRequest._ENDPOINT)
        .thenApply(bak -> bak.map(_bak -> _bak.withOperation(Backup.Operation.RESTORE)));
  }

  /**
   * List backups in the backend storage.
   *
   * @param backend Backup storage backend.
   */
  public CompletableFuture<List<Backup>> list(String backend) {
    return this.restTransport.performRequestAsync(ListBackupsRequest.of(backend), ListBackupsRequest._ENDPOINT);
  }

  /**
   * List backups in the backend storage.
   *
   * @param backend Backup storage backend.
   * @param fn      Lambda expression for optional parameters.
   */
  public CompletableFuture<List<Backup>> list(String backend,
      Function<ListBackupsRequest.Builder, ObjectBuilder<ListBackupsRequest>> fn) {
    return this.restTransport.performRequestAsync(ListBackupsRequest.of(backend, fn), ListBackupsRequest._ENDPOINT);
  }

  /**
   * Cancel in-progress backup.
   *
   * <p>
   * This method cannot be called cancel backup restore.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   */
  public CompletableFuture<Void> cancel(String backupId, String backend) {
    return this.restTransport.performRequestAsync(new CancelBackupRequest(backupId, backend),
        CancelBackupRequest._ENDPOINT);
  }
}
