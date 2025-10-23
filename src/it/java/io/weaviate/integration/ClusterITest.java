package io.weaviate.integration;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.cluster.ShardingState;
import io.weaviate.containers.Weaviate;

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
}
