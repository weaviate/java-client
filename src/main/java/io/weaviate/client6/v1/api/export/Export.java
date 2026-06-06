package io.weaviate.client6.v1.api.export;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.WeaviateClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Export(
    /** Export ID. */
    @SerializedName("id") String id,
    /** Path to export in the backend storage. */
    @SerializedName("path") String path,
    /** Export storage backend. */
    @SerializedName("backend") String backend,
    /** Collections included in the export. */
    @SerializedName("classes") List<String> includesCollections,
    /** Export creation status. */
    @SerializedName("status") ExportStatus status,
    /** Time at which the export creation. */
    @SerializedName("startedAt") OffsetDateTime startedAt,

    /**
     * Time at which the export was completed, successfully or otherwise.
     * Null unless export has completed.
     */
    @SerializedName("completedAt") OffsetDateTime completedAt,
    /**
     * Export creation error.
     * Null unless export has failed.
     */
    @SerializedName("error") String error,
    /**
     * Progress reports for individual shards within each collection.
     * Keyed by the name of the collection and then by shard ID.
     */
    @SerializedName("shardStatus") Map<String, Map<String, ShardExportProgress>> shardStatus,
    /**
     * Export duration in milliseconds.
     * Null unless export has completed.
     */
    @SerializedName("tookInMs") Integer tookMs) {

  /**
   * Block until the export has been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   * @throws TimeoutException      in case the wait times out without reaching
   *                               ExportStatus.SUCCESS.
   * @throws IOException           in case the request was not sent successfully
   *                               due to a malformed request, a networking error
   *                               or the server being unavailable.
   */
  public Export waitForCompletion(WeaviateClient client) throws IOException, TimeoutException {
    return waitForStatus(client, ExportStatus.SUCCESS);
  }

  /**
   * Block until the export has been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @param fn     Lambda expression for optional parameters.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   * @throws TimeoutException      in case the wait times out without reaching
   *                               ExportStatus.SUCCESS.
   * @throws IOException           in case the request was not sent successfully
   *                               due to a malformed request, a networking error
   *                               or the server being unavailable.
   */
  public Export waitForCompletion(WeaviateClient client, Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn)
      throws IOException, TimeoutException {
    return waitForStatus(client, ExportStatus.SUCCESS, fn);
  }

  /**
   * Block until the export operation reaches a certain status.
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
  public Export waitForStatus(WeaviateClient client, ExportStatus status) throws IOException, TimeoutException {
    return waitForStatus(client, status, ObjectBuilder.identity());
  }

  /**
   * Block until the export operation reaches a certain status.
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
  public Export waitForStatus(WeaviateClient client, ExportStatus status,
      Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn) throws IOException, TimeoutException {
    return new Waiter(this, WaitOptions.of(fn))
        .waitForStatus(status, () -> client.export.getCreateStatus(id, backend));
  }

  /**
   * Cancel export creation.
   *
   * <p>
   * This method cannot be called to cancel export restore.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClient#close}
   *               is NOT called before this method returns.
   * @throws IOException in case the request was not sent successfully
   *                     due to a malformed request, a networking error
   *                     or the server being unavailable.
   */
  public void cancel(WeaviateClient client) throws IOException {
    client.export.cancel(id(), backend());
  }

  /**
   * Poll until export's been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @throws IllegalStateException if {@link #operation} is not set (null).
   */
  public CompletableFuture<Export> waitForCompletion(WeaviateClientAsync client) {
    return waitForStatus(client, ExportStatus.SUCCESS);
  }

  /**
   * Poll until export's been created / restored successfully.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @param fn     Lambda expression for optional parameters.
   */
  public CompletableFuture<Export> waitForCompletion(WeaviateClientAsync client,
      Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn) {
    return waitForStatus(client, ExportStatus.SUCCESS, fn);
  }

  /**
   * Poll until export reaches a certain status or the wait times out.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @param status Target status.
   */
  public CompletableFuture<Export> waitForStatus(WeaviateClientAsync client, ExportStatus status) {
    return waitForStatus(client, status, ObjectBuilder.identity());
  }

  /**
   * Poll until export reaches a certain status or the wait times out.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   * @param status Target status.
   * @param fn     Lambda expression for optional parameters.
   */
  public CompletableFuture<Export> waitForStatus(WeaviateClientAsync client, ExportStatus status,
      Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn) {

    return new Waiter(this, WaitOptions.of(fn))
        .waitForStatusAsync(status, () -> client.export.getCreateStatus(id, backend));
  }

  /**
   * Cancel export creation.
   *
   * <p>
   * This method cannot be called to cancel export restore.
   *
   * @param client Weaviate client. Make sure {@link WeaviateClientAsync#close}
   *               is NOT called before this method returns.
   */
  public CompletableFuture<Void> cancel(WeaviateClientAsync client) {
    return client.export.cancel(id(), backend());
  }
}
