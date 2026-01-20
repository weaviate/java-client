package io.weaviate.client6.v1.api.cluster.replication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateReplicationClient {
  private final RestTransport restTransport;

  public WeaviateReplicationClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Get information about a replication operation.
   *
   * @param uuid Replication UUID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<Replication> get(UUID uuid) throws IOException {
    return this.restTransport.performRequest(GetReplicationRequest.of(uuid), GetReplicationRequest._ENDPOINT);
  }

  /**
   * Get information about a replication operation.
   *
   * @param uuid Replication UUID.
   * @param fn   Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<Replication> get(UUID uuid,
      Function<GetReplicationRequest.Builder, ObjectBuilder<GetReplicationRequest>> fn) throws IOException {
    return this.restTransport.performRequest(GetReplicationRequest.of(uuid, fn), GetReplicationRequest._ENDPOINT);
  }

  /**
   * List all replication operations.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   * @see WeaviateReplicationClient#list(Function) for filtering replications by
   *      collection, shard, or target node.
   */
  public List<Replication> list()
      throws IOException {
    return this.restTransport.performRequest(ListReplicationsRequest.of(), ListReplicationsRequest._ENDPOINT);
  }

  /**
   * List all replication operations.
   *
   * @param fn Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Replication> list(Function<ListReplicationsRequest.Builder, ObjectBuilder<ListReplicationsRequest>> fn)
      throws IOException {
    return this.restTransport.performRequest(ListReplicationsRequest.of(fn), ListReplicationsRequest._ENDPOINT);
  }

  /**
   * Cancel a replication operation.
   *
   * @param uuid Replication UUID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void cancel(UUID uuid)
      throws IOException {
    this.restTransport.performRequest(new CancelReplicationRequest(uuid), CancelReplicationRequest._ENDPOINT);
  }

  /**
   * Delete a replication operation.
   *
   * @param uuid Replication UUID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(UUID uuid)
      throws IOException {
    this.restTransport.performRequest(new DeleteReplicationRequest(uuid), DeleteReplicationRequest._ENDPOINT);
  }

  /**
   * Delete all replication operations.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void deleteAll()
      throws IOException {
    this.restTransport.performRequest(null, DeleteAllReplicationsRequest._ENDPOINT);
  }
}
