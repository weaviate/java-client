package io.weaviate.client6.v1.api.cluster;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateClusterClient {
  private final RestTransport restTransport;

  public WeaviateClusterClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Query sharding state of a collection.
   *
   * @param collection Collection name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<ShardingState> shardingState(String collection) throws IOException {
    return this.restTransport.performRequest(ListShardsRequest.of(collection), ListShardsRequest._ENDPOINT);
  }

  /**
   * Query sharding state of a collection.
   *
   * @param collection Collection name.
   * @param fn         Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<ShardingState> shardingState(String collection,
      Function<ListShardsRequest.Builder, ObjectBuilder<ListShardsRequest>> fn)
      throws IOException {
    return this.restTransport.performRequest(ListShardsRequest.of(collection, fn), ListShardsRequest._ENDPOINT);
  }

  /**
   * Get the status of all nodes in the cluster.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Node> listNodes()
      throws IOException {
    return this.restTransport.performRequest(ListNodesRequest.of(), ListNodesRequest._ENDPOINT);
  }

  /**
   * Get the status of all nodes in the cluster.
   *
   * @param fn Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Node> listNodes(Function<ListNodesRequest.Builder, ObjectBuilder<ListNodesRequest>> fn)
      throws IOException {
    return this.restTransport.performRequest(ListNodesRequest.of(fn), ListNodesRequest._ENDPOINT);
  }
}
