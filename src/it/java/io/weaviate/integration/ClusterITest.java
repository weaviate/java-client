package io.weaviate.integration;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.cluster.Node;
import io.weaviate.client6.v1.api.cluster.NodeVerbosity;
import io.weaviate.client6.v1.api.cluster.ShardingState;
import io.weaviate.client6.v1.api.cluster.replication.Replication;
import io.weaviate.client6.v1.api.cluster.replication.ReplicationState;
import io.weaviate.client6.v1.api.cluster.replication.ReplicationType;
import io.weaviate.containers.Weaviate;
import io.weaviate.containers.Weaviate.Version;

public class ClusterITest extends ConcurrentTest {
  private static final WeaviateClient client = Weaviate.cluster(3).getClient();

  @Test
  public void test_shardingState() throws IOException {
    // Arrange
    var nsA = ns("A");
    var nsB = ns("B");

    client.collections.create(nsA,
        a -> a.replication(r -> r.replicationFactor(2)));
    client.collections.create(nsB,
        b -> b.replication(r -> r.replicationFactor(3)));

    // Act
    var optShardsA = client.cluster.shardingState(nsA);
    var optShardsB = client.cluster.shardingState(nsB);

    // Assert
    var shardsA = Assertions.assertThat(optShardsA).get()
        .returns(nsA, ShardingState::collection)
        .extracting(ShardingState::shards)
        .actual();

    var shardsB = Assertions.assertThat(optShardsB).get()
        .returns(nsB, ShardingState::collection)
        .extracting(ShardingState::shards)
        .actual();

    Assertions.assertThat(shardsA).doesNotContainAnyElementsOf(shardsB);
  }

  @Test
  public void test_listNodes() throws IOException {
    // Act
    var allNodes = client.cluster.listNodes();

    // Assert
    Assertions.assertThat(allNodes).as("total no. nodes").hasSize(3);
  }

  @Test
  public void test_replicateLifecycle() throws IOException {
    Version.V132.orSkip();

    // Arrange

    // We must create the collection first before any shards exist on the nodes.
    var nsThings = ns("Things");
    client.collections.create(nsThings, c -> c.replication(r -> r.replicationFactor(2)));

    var nodes = client.cluster.listNodes(opt -> opt.verbosity(NodeVerbosity.VERBOSE));
    Assertions.assertThat(nodes)
        .as("cluster at least 2 nodes").hasSizeGreaterThanOrEqualTo(2);

    Node source = null;
    Node target = null;
    for (var node : nodes) {
      if (source == null && !node.shards().isEmpty()) {
        source = node;
      } else if (target == null) {
        target = node;
      }
    }

    var wantShard = source.shards().get(0).name();
    var srcNode = source.name();
    var tgtNode = target.name();

    // Act: start replication
    var replication = client.cluster.replicate(
        nsThings,
        wantShard,
        srcNode,
        tgtNode,
        ReplicationType.MOVE);

    var got = client.cluster.replication.get(replication.uuid());
    Assertions.assertThat(got).get()
        .as("expected replication status")
        .returns(nsThings, Replication::collection)
        .returns(wantShard, Replication::shard)
        .returns(srcNode, Replication::sourceNode)
        .returns(tgtNode, Replication::targetNode)
        .returns(ReplicationType.MOVE, Replication::type)
        .returns(null, Replication::history)
        .extracting(Replication::status).isNotNull();

    var withHistory = client.cluster.replication.get(
        replication.uuid(),
        repl -> repl.includeHistory(true));
    Assertions.assertThat(withHistory).get()
        .as("includes history")
        .extracting(Replication::history).isNotNull();

    // Act: query replications
    var filtered = client.cluster.replication.list(
        repl -> repl
            .collection(nsThings)
            .shard(wantShard)
            .targetNode(tgtNode));

    Assertions.assertThat(filtered)
        .as("existing replications for %s-%s -> %s", nsThings, wantShard, tgtNode)
        .hasSize(1);

    // Act: cancel
    client.cluster.replication.cancel(replication.uuid());

    eventually(() -> client.cluster.replication.get(replication.uuid())
        .orElseThrow()
        .status().state() == ReplicationState.CANCELED, 1000, 25, "replication must be canceled");

    // Act: delete replication
    client.cluster.replication.delete(replication.uuid());

    eventually(() -> client.cluster.replication.list().isEmpty(), 1000, 15, "replication must be deleted");
  }
}
