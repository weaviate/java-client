package io.weaviate.client6.v1.api.cluster.replication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateReplicationClientAsync {
  private final RestTransport restTransport;

  public WeaviateReplicationClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Get information about a replication operation.
   *
   * @param uuid Replication UUID.
   */
  public CompletableFuture<Optional<Replication>> get(UUID uuid) throws IOException {
    return this.restTransport.performRequestAsync(GetReplicationRequest.of(uuid), GetReplicationRequest._ENDPOINT);
  }

  /**
   * Get information about a replication operation.
   *
   * @param uuid Replication UUID.
   * @param fn   Lambda expression for optional parameters.
   */
  public CompletableFuture<Optional<Replication>> get(UUID uuid,
      Function<GetReplicationRequest.Builder, ObjectBuilder<GetReplicationRequest>> fn) throws IOException {
    return this.restTransport.performRequestAsync(GetReplicationRequest.of(uuid, fn), GetReplicationRequest._ENDPOINT);
  }

  /**
   * List all replication operations.
   *
   * @see WeaviateReplicationClientAsync#list(Function) for filtering replications
   *      by
   *      collection, shard, or target node.
   */
  public CompletableFuture<List<Replication>> list()
      throws IOException {
    return this.restTransport.performRequestAsync(ListReplicationsRequest.of(), ListReplicationsRequest._ENDPOINT);
  }

  /**
   * List all replication operations.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public CompletableFuture<List<Replication>> list(
      Function<ListReplicationsRequest.Builder, ObjectBuilder<ListReplicationsRequest>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(ListReplicationsRequest.of(fn), ListReplicationsRequest._ENDPOINT);
  }

  /**
   * Cancel a replication operation.
   *
   * @param uuid Replication UUID.
   */
  public CompletableFuture<Void> cancel(UUID uuid)
      throws IOException {
    return this.restTransport.performRequestAsync(new CancelReplicationRequest(uuid),
        CancelReplicationRequest._ENDPOINT);
  }

  /**
   * Delete a replication operation.
   *
   * @param uuid Replication UUID.
   */
  public CompletableFuture<Void> delete(UUID uuid)
      throws IOException {
    return this.restTransport.performRequestAsync(new DeleteReplicationRequest(uuid),
        DeleteReplicationRequest._ENDPOINT);
  }

  /** Delete all replication operations. */
  public CompletableFuture<Void> deleteAll()
      throws IOException {
    return this.restTransport.performRequestAsync(null, DeleteAllReplicationsRequest._ENDPOINT);
  }
}
