package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateBackupClient {
  private final RestTransport restTransport;

  public WeaviateBackupClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Start a new backup process.
   *
   * @param backupId Backup ID. Must be unique for the backend.
   * @param backend  Backup storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Backup create(String backupId, String backend) throws IOException {
    return create(new CreateBackupRequest(CreateBackupRequest.BackupCreate.of(backupId), backend));
  }

  /**
   * Start a new backup process.
   *
   * @param backupId Backup ID. Must be unique for the backend.
   * @param backend  Backup storage backend.
   * @param fn       Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Backup create(String backupId, String backend,
      Function<CreateBackupRequest.BackupCreate.Builder, ObjectBuilder<CreateBackupRequest.BackupCreate>> fn)
      throws IOException {
    return create(new CreateBackupRequest(CreateBackupRequest.BackupCreate.of(backupId, fn), backend));
  }

  /**
   * Start a new backup process.
   *
   * @param request Create backup request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Backup create(CreateBackupRequest request) throws IOException {
    return this.restTransport.performRequest(request, CreateBackupRequest._ENDPOINT)
        .withOperation(Backup.Operation.CREATE);
  }

  /**
   * Get backup create status.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<Backup> getCreateStatus(String backupId, String backend) throws IOException {
    return this.restTransport.performRequest(
        new GetCreateStatusRequest(backupId, backend), GetCreateStatusRequest._ENDPOINT)
        .map(b -> b.withOperation(Backup.Operation.CREATE));
  }

  /**
   * Start backup restore process.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Backup restore(String backupId, String backend) throws IOException {
    return restore(new RestoreBackupRequest(backupId, backend, RestoreBackupRequest.BackupRestore.of()));
  }

  /**
   * Start backup restore process.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   * @param fn       Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Backup restore(String backupId, String backend,
      Function<RestoreBackupRequest.BackupRestore.Builder, ObjectBuilder<RestoreBackupRequest.BackupRestore>> fn)
      throws IOException {
    return restore(new RestoreBackupRequest(backupId, backend, RestoreBackupRequest.BackupRestore.of(fn)));
  }

  /**
   * Start backup restore process.
   *
   * @param request Restore backup request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Backup restore(RestoreBackupRequest request) throws IOException {
    return this.restTransport.performRequest(request, RestoreBackupRequest._ENDPOINT)
        .withOperation(Backup.Operation.RESTORE);
  }

  /**
   * Get backup restore status.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<Backup> getRestoreStatus(String backupId, String backend) throws IOException {
    return this.restTransport
        .performRequest(new GetRestoreStatusRequest(backupId, backend), GetRestoreStatusRequest._ENDPOINT)
        .map(b -> b.withOperation(Backup.Operation.RESTORE));
  }

  /**
   * List backups in the backend storage.
   *
   * @param backend Backup storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Backup> list(String backend) throws IOException {
    return this.restTransport.performRequest(new ListBackupsRequest(backend), ListBackupsRequest._ENDPOINT);
  }

  /**
   * Cancel in-progress backup.
   *
   * @param backupId Backup ID.
   * @param backend  Backup storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void cancel(String backupId, String backend) throws IOException {
    this.restTransport.performRequest(new CancelBackupRequest(backupId, backend), CancelBackupRequest._ENDPOINT);
  }
}
