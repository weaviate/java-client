package io.weaviate.integration.client.cluster;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.cluster.api.replication.Replication;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperation;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperationState;
import io.weaviate.client.v1.cluster.model.NodeStatusOutput;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import io.weaviate.client.v1.cluster.model.ReplicationType;
import io.weaviate.client.v1.cluster.model.ShardReplicas;
import io.weaviate.client.v1.cluster.model.ShardingState;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerComposeCluster;

public class ClientReplicateTest {
  @ClassRule
  public static WeaviateDockerComposeCluster cluster = new WeaviateDockerComposeCluster();

  private static WeaviateClient client;

  @Before
  public void before() {
    Config config = new Config("http", cluster.getHttpHost0Address());
    client = new WeaviateClient(config);
  }

  private static final String CLASSNAME = "ShardDweller";

  @After
  public void afterEach() {
    client.schema().classDeleter().withClassName(CLASSNAME).run();
  }

  @Test
  public void testQueryShardingState() {
    // Arrange
    Boolean created = client.schema().classCreator()
        .withClass(WeaviateClass.builder().className(CLASSNAME).build())
        .run().getResult();
    assumeTrue(created, "created test collection");

    NodesStatusResponse nodes = client.cluster().nodesStatusGetter()
        .withClassName(CLASSNAME)
        .withOutput(NodeStatusOutput.VERBOSE)
        .run().getResult();

    assumeTrue(nodes != null, "nodes status result is not null");
    assumeTrue(!Arrays.isArrayEmpty(nodes.getNodes()), "there're 1+ nodes in the cluster");
    String wantShard = nodes.getNodes()[0].getShards()[0].getName();

    ShardingState shardingState;

    // Act: query by collection name
    shardingState = client.cluster().shardingStateQuerier()
        .withClassName(CLASSNAME)
        .run().getResult();
    Assertions.assertThat(shardingState.getShards())
        .as("shard present in the sharding state output (by collection)")
        .extracting(ShardReplicas::getName).contains(wantShard);

    // Act: query by collection + shard name
    shardingState = client.cluster().shardingStateQuerier()
        .withClassName(CLASSNAME)
        .withShard(wantShard)
        .run().getResult();
    Assertions.assertThat(shardingState.getShards())
        .as("shard present in the sharding state output (by collection+shard)")
        .extracting(ShardReplicas::getName).contains(wantShard);

    ShardingState inexistent;
    // Act: query inexistent
    inexistent = client.cluster().shardingStateQuerier()
        .withClassName("Unknown")
        .run().getResult();
    Assertions.assertThat(inexistent).isNull();
  }

  @Test
  /**
   * This test starts a replication operation between two nodes,
   * queries for its status, then cancels the replication and eventually deletes
   * it.
   *
   * Note that assertions that use {@link #eventually} helper may be flaky.
   */
  public void testReplicateLifecycle() {
    // Arrange
    Boolean created = client.schema().classCreator()
        .withClass(WeaviateClass.builder().className(CLASSNAME).build())
        .run().getResult();
    assumeTrue(created, "created test collection");

    NodesStatusResponse nodes = client.cluster().nodesStatusGetter()
        .withClassName(CLASSNAME)
        .withOutput(NodeStatusOutput.VERBOSE)
        .run().getResult();

    assumeTrue(nodes != null, "nodes status result is not null");
    assumeTrue(nodes.getNodes().length >= 2, "there're 2+ nodes in the cluster");

    String srcNode = nodes.getNodes()[0].getName();
    String tgtNode = nodes.getNodes()[1].getName();
    String wantShard = nodes.getNodes()[0].getShards()[0].getName();

    deleteAllReplications(5);

    // Act: kick-off replication
    String uuid = client.cluster().replicator()
        .withClassName(CLASSNAME)
        .withShard(wantShard)
        .withSourceNode(srcNode)
        .withTargetNode(tgtNode)
        .run().getResult();
    assumeTrue(uuid != null, "replication started with valid uuid");

    // Act: get status
    ReplicateOperation status_1 = client.cluster().replication().getter()
        .withUuid(uuid).run().getResult();

    Assertions.assertThat(status_1).isNotNull()
        .as("expected replication status")
        .returns(CLASSNAME, ReplicateOperation::getClassName)
        .returns(wantShard, ReplicateOperation::getShard)
        .returns(srcNode, ReplicateOperation::getSourceNode)
        .returns(tgtNode, ReplicateOperation::getTargetNode)
        .returns(ReplicationType.COPY, ReplicateOperation::getTransferType)
        .returns(null, ReplicateOperation::getStatusHistory)
        .extracting(ReplicateOperation::getStatus).isNotNull();

    // Act: get status with history
    ReplicateOperation status_2 = client.cluster().replication().getter()
        .withUuid(uuid).withIncludeHistory(true)
        .run().getResult();

    Assertions.assertThat(status_2).isNotNull()
        .as("includes replication status history")
        .extracting(ReplicateOperation::getStatusHistory).isNotNull();

    // Act: query status
    List<ReplicateOperation> operations = client.cluster().replication().querier()
        .withClassName(CLASSNAME).withShard(wantShard).withTargetNode(tgtNode)
        .run().getResult();

    Assertions.assertThat(operations).as("no. replications").hasSize(1);

    // Act: cancel
    Result<Boolean> cancel = client.cluster().replication().canceler().withUuid(uuid).run();
    Assertions.assertThat(cancel).as("cancel error").returns(null, Result::getError);

    eventually(() -> client.cluster().replication().getter().withUuid(uuid).run().getResult()
        .getStatus().getState() == ReplicateOperationState.CANCELLED,
        25, "replication was not cancelled");

    // Act: delete
    Result<Boolean> delete = client.cluster().replication().deleter().withUuid(uuid).run();
    Assertions.assertThat(delete).as("delete error").returns(null, Result::getError);

    eventually(() -> client.cluster().replication().allGetter().run().getResult().isEmpty(),
        15, "replication was not deleted");
  }

  private static void deleteAllReplications(int timeoutSeconds) {
    Replication replication = client.cluster().replication();
    replication.allDeleter().run();
    eventually(() -> replication.allGetter().run().getResult().isEmpty(),
        timeoutSeconds,
        "did not delete existing replications");
  }

  private static void eventually(Supplier<Boolean> cond, int timeoutSeconds, String... message) {
    CompletableFuture<?> check = CompletableFuture.runAsync(() -> {
      while (!Thread.currentThread().isInterrupted() && !cond.get()) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    });

    try {
      check.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException ex) {
      check.cancel(true);
      Assertions.fail(message.length >= 0 ? message[0] : null, ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      Assertions.fail(ex);
    } catch (ExecutionException ex) {
      throw new RuntimeException(ex);
    }
  }
}
