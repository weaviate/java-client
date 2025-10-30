package io.weaviate.client6.v1.api.cluster;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.cluster.replication.CreateReplicationRequest;
import io.weaviate.client6.v1.api.cluster.replication.Replication;
import io.weaviate.client6.v1.api.cluster.replication.ReplicationType;
import io.weaviate.client6.v1.api.cluster.replication.WeaviateReplicationClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateClusterClientAsync {
  private final RestTransport restTransport;

  public final WeaviateReplicationClientAsync replication;

  public WeaviateClusterClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
    this.replication = new WeaviateReplicationClientAsync(restTransport);
  }

  /**
   * Query sharding state of a collection.
   *
   * @param collection Collection name.
   */
  public CompletableFuture<Optional<ShardingState>> shardingState(String collection) throws IOException {
    return this.restTransport.performRequestAsync(ListShardsRequest.of(collection), ListShardsRequest._ENDPOINT);
  }

  /**
   * Query sharding state of a collection.
   *
   * @param collection Collection name.
   * @param fn         Lambda expression for optional parameters.
   */
  public CompletableFuture<Optional<ShardingState>> shardingState(String collection,
      Function<ListShardsRequest.Builder, ObjectBuilder<ListShardsRequest>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(ListShardsRequest.of(collection, fn), ListShardsRequest._ENDPOINT);
  }

  /**
   * Get the status of all nodes in the cluster.
   */
  public CompletableFuture<List<Node>> listNodes()
      throws IOException {
    return this.restTransport.performRequestAsync(ListNodesRequest.of(), ListNodesRequest._ENDPOINT);
  }

  /**
   * Get the status of all nodes in the cluster.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public CompletableFuture<List<Node>> listNodes(Function<ListNodesRequest.Builder, ObjectBuilder<ListNodesRequest>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(ListNodesRequest.of(fn), ListNodesRequest._ENDPOINT);
  }

  /**
   * Start a replication operation for a collection's shard.
   *
   * @param collection Collection name.
   * @param shard      Shard name.
   * @param sourceNode Node on which the shard currently resides.
   * @param targetNode Node onto which the files will be replicated.
   */
  public CompletableFuture<Replication> replicate(
      String collection,
      String shard,
      String sourceNode,
      String targetNode,
      ReplicationType type) {
    return this.restTransport.performRequestAsync(
        new CreateReplicationRequest(collection, shard, sourceNode, targetNode, type),
        CreateReplicationRequest._ENDPOINT);
  }
}
