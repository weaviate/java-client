package technology.semi.weaviate.integration.client.cluster;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.cluster.model.NodesStatusResponse;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;
import static org.assertj.core.api.Assertions.assertThat;
import static technology.semi.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_GIT_HASH;
import static technology.semi.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class ClientClusterTest {

  @ClassRule
  public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
  private WeaviateClient client;

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
    assertThat(nodeStatus.getShards()).hasSize(0);
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
}
