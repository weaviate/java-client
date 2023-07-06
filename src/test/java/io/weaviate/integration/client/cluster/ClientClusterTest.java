package io.weaviate.integration.client.cluster;

import io.weaviate.client.base.util.TriConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import io.weaviate.integration.client.WeaviateTestGenerics;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_GIT_HASH;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

public class ClientClusterTest {

  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    Config config = new Config("http", host + ":" + port);

    client = new WeaviateClient(config);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testClusterNodesEndpointWithoutData() {
    // when
    Result<NodesStatusResponse> result = client.cluster().nodesStatusGetter().run();

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
  public void testClusterNodesEndpointWithData() {
    // given
    testGenerics.createTestSchemaAndData(client);

    // when
    Result<NodesStatusResponse> result = client.cluster().nodesStatusGetter().run();

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
  public void shouldGetNodeStatusPerClass() {
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    testGenerics.createSchemaPizza(client);
    testGenerics.createDataPizza(client);
    testGenerics.createSchemaSoup(client);
    testGenerics.createDataSoup(client);

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
      .run();

    assertSingleNode.accept(resultAll);
    assertCounts.accept(resultAll.getResult().getNodes()[0], 2L, (long) (pizzaIds.size() + soupIds.size()));

    // PIZZA
    Result<NodesStatusResponse> resultPizza = client.cluster()
      .nodesStatusGetter()
      .withClassName("Pizza")
      .run();

    assertSingleNode.accept(resultPizza);
    assertCounts.accept(resultPizza.getResult().getNodes()[0], 1L, (long) pizzaIds.size());

    // SOUP
    Result<NodesStatusResponse> resultSoup = client.cluster()
      .nodesStatusGetter()
      .withClassName("Soup")
      .run();

    assertSingleNode.accept(resultSoup);
    assertCounts.accept(resultSoup.getResult().getNodes()[0], 1L, (long) soupIds.size());
  }
}
