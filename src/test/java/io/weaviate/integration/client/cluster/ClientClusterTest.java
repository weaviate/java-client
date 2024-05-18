package io.weaviate.integration.client.cluster;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.TriConsumer;
import io.weaviate.client.v1.cluster.model.NodeStatusOutput;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_GIT_HASH;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;
import java.util.List;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientClusterTest {

  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testClusterNodesEndpointWithoutDataWithOutputVerbose() {
    // when
    Result<NodesStatusResponse> result = client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .run();

    // then
    assertThat(result).isNotNull();
    assertThat(result.hasErrors()).isFalse();

    NodesStatusResponse nodes = result.getResult();
    assertThat(nodes).isNotNull();
    assertThat(nodes.getNodes()).hasSize(1);

    NodesStatusResponse.NodeStatus nodeStatus = nodes.getNodes()[0];
    assertThat(nodeStatus.getName()).isNotBlank();
    assertThat(nodeStatus.getShards()).isNull();
    assertThat(nodeStatus)
      .returns(EXPECTED_WEAVIATE_VERSION, NodesStatusResponse.NodeStatus::getVersion)
      .returns(EXPECTED_WEAVIATE_GIT_HASH, NodesStatusResponse.NodeStatus::getGitHash)
      .returns(NodesStatusResponse.Status.HEALTHY, NodesStatusResponse.NodeStatus::getStatus)
      .extracting(NodesStatusResponse.NodeStatus::getStats)
      .returns(0L, NodesStatusResponse.Stats::getShardCount)
      .returns(0L, NodesStatusResponse.Stats::getObjectCount);
  }

  @Test
  public void testClusterNodesEndpointWithDataWithOutputVerbose() throws InterruptedException {
    // given
    testGenerics.createTestSchemaAndData(client);
    Thread.sleep(3000); // makes sure data are flushed so nodes endpoint returns actual object/shard count

    // when
    Result<NodesStatusResponse> result = client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .run();

    // then
    assertThat(result).isNotNull();
    assertThat(result.hasErrors()).isFalse();

    NodesStatusResponse nodes = result.getResult();
    assertThat(nodes).isNotNull();
    assertThat(nodes.getNodes()).hasSize(1);

    NodesStatusResponse.NodeStatus nodeStatus = nodes.getNodes()[0];
    assertThat(nodeStatus.getName()).isNotBlank();
    assertThat(nodeStatus)
      .returns(EXPECTED_WEAVIATE_VERSION, NodesStatusResponse.NodeStatus::getVersion)
      .returns(EXPECTED_WEAVIATE_GIT_HASH, NodesStatusResponse.NodeStatus::getGitHash)
      .returns(NodesStatusResponse.Status.HEALTHY, NodesStatusResponse.NodeStatus::getStatus)
      .extracting(NodesStatusResponse.NodeStatus::getStats)
      .returns(2L, NodesStatusResponse.Stats::getShardCount)
      .returns(6L, NodesStatusResponse.Stats::getObjectCount);

    assertThat(nodeStatus.getShards()).hasSize(2)
      .extracting(NodesStatusResponse.ShardStatus::getClassName)
      .containsExactlyInAnyOrder("Pizza", "Soup");

    for (NodesStatusResponse.ShardStatus shardStatus : nodeStatus.getShards()) {
      assertThat(shardStatus.getName()).isNotBlank();
      switch (shardStatus.getClassName()) {
        case "Pizza":
          assertThat(shardStatus.getObjectCount()).isEqualTo(4L);
          break;
        case "Soup":
          assertThat(shardStatus.getObjectCount()).isEqualTo(2L);
          break;
      }
    }
  }

  @Test
  public void shouldGetNodeStatusPerClassWithOutputVerbose() throws InterruptedException {
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    testGenerics.createSchemaPizza(client);
    testGenerics.createDataPizza(client);
    testGenerics.createSchemaSoup(client);
    testGenerics.createDataSoup(client);
    Thread.sleep(3000); // makes sure data are flushed so nodes endpoint returns actual object/shard count

    Consumer<Result<NodesStatusResponse>> assertSingleNode = (Result<NodesStatusResponse> result) ->
      assertThat(result).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull()
        .extracting(NodesStatusResponse::getNodes).asInstanceOf(ARRAY)
        .hasSize(1);

    TriConsumer<NodesStatusResponse.NodeStatus, Long, Long> assertCounts = (NodesStatusResponse.NodeStatus nodeStatus, Long shardCount, Long objectCount) -> {
      assertThat(nodeStatus.getName()).isNotBlank();
      assertThat(nodeStatus)
        .returns(EXPECTED_WEAVIATE_VERSION, NodesStatusResponse.NodeStatus::getVersion)
        .returns(EXPECTED_WEAVIATE_GIT_HASH, NodesStatusResponse.NodeStatus::getGitHash)
        .returns(NodesStatusResponse.Status.HEALTHY, NodesStatusResponse.NodeStatus::getStatus)
        .extracting(NodesStatusResponse.NodeStatus::getStats)
        .returns(shardCount, NodesStatusResponse.Stats::getShardCount)
        .returns(objectCount, NodesStatusResponse.Stats::getObjectCount);
    };

    // ALL
    Result<NodesStatusResponse> resultAll = client.cluster()
      .nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .run();

    assertSingleNode.accept(resultAll);
    assertCounts.accept(resultAll.getResult().getNodes()[0], 2L, (long) (pizzaIds.size() + soupIds.size()));

    // PIZZA
    Result<NodesStatusResponse> resultPizza = client.cluster()
      .nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .withClassName("Pizza")
      .run();

    assertSingleNode.accept(resultPizza);
    assertCounts.accept(resultPizza.getResult().getNodes()[0], 1L, (long) pizzaIds.size());

    // SOUP
    Result<NodesStatusResponse> resultSoup = client.cluster()
      .nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .withClassName("Soup")
      .run();

    assertSingleNode.accept(resultSoup);
    assertCounts.accept(resultSoup.getResult().getNodes()[0], 1L, (long) soupIds.size());
  }

  @Test
  public void testClusterNodesEndpointWithOutputMinimalImplicit() {
    // when
    Result<NodesStatusResponse> result = client.cluster().nodesStatusGetter()
      .run();

    // then
    assertThat(result).isNotNull();
    assertThat(result.hasErrors()).isFalse();

    NodesStatusResponse nodes = result.getResult();
    assertThat(nodes).isNotNull();
    assertThat(nodes.getNodes()).hasSize(1);

    NodesStatusResponse.NodeStatus nodeStatus = nodes.getNodes()[0];
    assertThat(nodeStatus.getName()).isNotBlank();
    assertThat(nodeStatus)
      .returns(EXPECTED_WEAVIATE_VERSION, NodesStatusResponse.NodeStatus::getVersion)
      .returns(EXPECTED_WEAVIATE_GIT_HASH, NodesStatusResponse.NodeStatus::getGitHash)
      .returns(NodesStatusResponse.Status.HEALTHY, NodesStatusResponse.NodeStatus::getStatus)
      .returns(null, NodesStatusResponse.NodeStatus::getStats)
      .returns(null, NodesStatusResponse.NodeStatus::getShards);
  }
}
