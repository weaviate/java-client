package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.WeaviateClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Backup(
    /** Backup ID. */
    @SerializedName("id") String id,
    /** Path to backup in the backend storage. */
    @SerializedName("path") String path,
    /** Backup storage backend. */
    @SerializedName("backend") String backend,
    /** Collections included in the backup. */
    @SerializedName("classes") List<String> includesCollections,
    /** Backup creation / restoration status. */
    @SerializedName("status") BackupStatus status,
    /** Backup creation / restoration error. */
    @SerializedName("error") String error,
    /** Time at which the backup creation. */
    @SerializedName("startedAt") OffsetDateTime startedAt,
    /** Time at which the backup was completed, successfully or otherwise. */
    @SerializedName("completedAt") OffsetDateTime completedAt,
    /** Backup size in GiB. */
    @SerializedName("size") Double sizeGiB,
    /**
     * This value indicates if a backup is being created or restored from.
     * For operations like LIST this value is null.
     */
    // We set a bogus SerializedName to make sure we do not pick up this
    // value from the JSON by accident, but always set it ourselves.
    @SerializedName("__operation__") Operation operation) {

  /** Set operation associated with this backup. */
  Backup withOperation(Operation operation) {
    return new Backup(
        id,
        path,
        backend,
        includesCollections,
        status,
        error,
        startedAt,
        completedAt,
        sizeGiB,
        operation);
  }

  public enum Operation {
    CREATE, RESTORE;
  }

  /**
   * Block until the backup has been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   * @throws TimeoutException      in case the wait times out without reaching
   *                               BackupStatus.SUCCESS.
   * @throws IOException           in case the request was not sent successfully
   *                               due to a malformed request, a networking error
   *                               or the server being unavailable.
   */
  public Backup waitForCompletion(WeaviateClient client) throws IOException, TimeoutException {
    return waitForStatus(client, BackupStatus.SUCCESS);
  }

  /**
   * Block until the backup has been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @param fn     Lambda expression for optional parameters.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   * @throws TimeoutException      in case the wait times out without reaching
   *                               BackupStatus.SUCCESS.
   * @throws IOException           in case the request was not sent successfully
   *                               due to a malformed request, a networking error
   *                               or the server being unavailable.
   */
  public Backup waitForCompletion(WeaviateClient client, Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn)
      throws IOException, TimeoutException {
    return waitForStatus(client, BackupStatus.SUCCESS, fn);
  }

  /**
   * Block until the backup operation reaches a certain status.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @param status Target status.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   * @throws TimeoutException      in case the wait times out without reaching
   *                               the target status.
   * @throws IOException           in case the request was not sent successfully
   *                               due to a malformed request, a networking error
   *                               or the server being unavailable.
   */
  public Backup waitForStatus(WeaviateClient client, BackupStatus status) throws IOException, TimeoutException {
    return waitForStatus(client, status, ObjectBuilder.identity());
  }

  /**
   * Block until the backup operation reaches a certain status.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @param status Target status.
   * @param fn     Lambda expression for optional parameters.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   * @throws TimeoutException      in case the wait times out without reaching
   *                               the target status.
   * @throws IOException           in case the request was not sent successfully
   *                               due to a malformed request, a networking error
   *                               or the server being unavailable.
   */
  public Backup waitForStatus(WeaviateClient client, BackupStatus status,
      Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn) throws IOException, TimeoutException {
    if (operation == null) {
      throw new IllegalStateException("backup.operation is null");
    }

    final var options = WaitOptions.of(fn);
    final Callable<Optional<Backup>> poll = operation == Operation.CREATE
        ? () -> client.backup.getCreateStatus(id, backend)
        : () -> client.backup.getRestoreStatus(id, backend);
    return new Waiter(this, options).waitForStatus(status, poll);
  }

  /**
   * Cancel backup creation.
   *
   * <p>
   * This method cannot be called to cancel backup restore.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @throws IOException in case the request was not sent successfully
   *                     due to a malformed request, a networking error
   *                     or the server being unavailable.
   */
  public void cancel(WeaviateClient client) throws IOException {
    client.backup.cancel(id(), backend());
  }

  /**
   * Poll until backup's been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   */
  public CompletableFuture<Backup> waitForCompletion(WeaviateClientAsync client) {
    return waitForStatus(client, BackupStatus.SUCCESS);
  }

  /**
   * Poll until backup's been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @param fn     Lambda expression for optional parameters.
   */
  public CompletableFuture<Backup> waitForCompletion(WeaviateClientAsync client,
      Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn) {
    return waitForStatus(client, BackupStatus.SUCCESS, fn);
  }

  /**
   * Poll until backup reaches a certain status or the wait times out.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @param status Target status.
   */
  public CompletableFuture<Backup> waitForStatus(WeaviateClientAsync client, BackupStatus status) {
    return waitForStatus(client, status, ObjectBuilder.identity());
  }

  /**
   * Poll until backup reaches a certain status or the wait times out.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @param status Target status.
   * @param fn     Lambda expression for optional parameters.
   */
  public CompletableFuture<Backup> waitForStatus(WeaviateClientAsync client, BackupStatus status,
      Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn) {
    if (operation == null) {
      throw new IllegalStateException("backup.operation is null");
    }

    final var options = WaitOptions.of(fn);
    final Supplier<CompletableFuture<Optional<Backup>>> poll = operation == Operation.CREATE
        ? () -> client.backup.getCreateStatus(id, backend)
        : () -> client.backup.getRestoreStatus(id, backend);
    return new Waiter(this, options).waitForStatusAsync(status, poll);
  }

  /**
   * Cancel backup creation.
   *
   * <p>
   * This method cannot be called to cancel backup restore.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   */
  public CompletableFuture<Void> cancel(WeaviateClientAsync client) {
    return client.backup.cancel(id(), backend());
  }
}
