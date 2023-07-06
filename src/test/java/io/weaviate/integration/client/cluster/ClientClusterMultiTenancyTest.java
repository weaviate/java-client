package io.weaviate.integration.client.cluster;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.TriConsumer;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import io.weaviate.integration.client.WeaviateTestGenerics;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_GIT_HASH;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

public class ClientClusterMultiTenancyTest {

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
  public void shouldGetNodeStatusPerClass() {
    String[] pizzaTenants = new String[]{"TenantPizza1", "TenantPizza2"};
    String[] soupTenants = new String[]{"TenantSoup1", "TenantSoup2", "TenantSoup3"};
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, pizzaTenants);
    testGenerics.createDataPizzaForTenants(client, pizzaTenants);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, soupTenants);
    testGenerics.createDataSoupForTenants(client, soupTenants);

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

    long expectedAllShardCount = pizzaTenants.length + soupTenants.length;
    long expectedAllObjectsCount = pizzaTenants.length * pizzaIds.size() + soupTenants.length * soupIds.size();
    assertSingleNode.accept(resultAll);
    assertCounts.accept(resultAll.getResult().getNodes()[0], expectedAllShardCount, expectedAllObjectsCount);

    // PIZZA
    Result<NodesStatusResponse> resultPizza = client.cluster()
      .nodesStatusGetter()
      .withClassName("Pizza")
      .run();

    long expectedPizzaShardCount = pizzaTenants.length;
    long expectedPizzaObjectsCount = pizzaTenants.length * pizzaIds.size();
    assertSingleNode.accept(resultPizza);
    assertCounts.accept(resultPizza.getResult().getNodes()[0], expectedPizzaShardCount, expectedPizzaObjectsCount);

    // SOUP
    Result<NodesStatusResponse> resultSoup = client.cluster()
      .nodesStatusGetter()
      .withClassName("Soup")
      .run();

    long expectedSoupShardCount = soupTenants.length;
    long expectedSoupObjectsCount = soupTenants.length * soupIds.size();
    assertSingleNode.accept(resultSoup);
    assertCounts.accept(resultSoup.getResult().getNodes()[0], expectedSoupShardCount, expectedSoupObjectsCount);
  }
}
