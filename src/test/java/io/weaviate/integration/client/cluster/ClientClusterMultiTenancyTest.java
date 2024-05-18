package io.weaviate.integration.client.cluster;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.TriConsumer;
import io.weaviate.client.v1.cluster.model.NodeStatusOutput;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_GIT_HASH;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientClusterMultiTenancyTest {

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
  public void shouldGetNodeStatusPerClass() throws InterruptedException {
    Tenant[] pizzaTenants = new Tenant[] {
      Tenant.builder().name("TenantPizza1").build(),
      Tenant.builder().name("TenantPizza2").build(),
    };
    Tenant[] soupTenants = new Tenant[] {
      Tenant.builder().name("TenantSoup1").build(),
      Tenant.builder().name("TenantSoup2").build(),
      Tenant.builder().name("TenantSoup3").build(),
    };
    String[] pizzaTenantNames = Arrays.stream(pizzaTenants).map(Tenant::getName).toArray(String[]::new);
    String[] soupTenantNames = Arrays.stream(soupTenants).map(Tenant::getName).toArray(String[]::new);

    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, pizzaTenants);
    testGenerics.createDataPizzaForTenants(client, pizzaTenantNames);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, soupTenants);
    testGenerics.createDataSoupForTenants(client, soupTenantNames);
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
    Result<NodesStatusResponse> resultAll = client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .run();

    long expectedAllShardCount = pizzaTenants.length + soupTenants.length;
    long expectedAllObjectsCount = pizzaTenants.length * pizzaIds.size() + soupTenants.length * soupIds.size();
    assertSingleNode.accept(resultAll);
    assertCounts.accept(resultAll.getResult().getNodes()[0], expectedAllShardCount, expectedAllObjectsCount);

    // PIZZA
    Result<NodesStatusResponse> resultPizza = client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .withClassName("Pizza")
      .run();

    long expectedPizzaShardCount = pizzaTenants.length;
    long expectedPizzaObjectsCount = pizzaTenants.length * pizzaIds.size();
    assertSingleNode.accept(resultPizza);
    assertCounts.accept(resultPizza.getResult().getNodes()[0], expectedPizzaShardCount, expectedPizzaObjectsCount);

    // SOUP
    Result<NodesStatusResponse> resultSoup = client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .withClassName("Soup")
      .run();

    long expectedSoupShardCount = soupTenants.length;
    long expectedSoupObjectsCount = soupTenants.length * soupIds.size();
    assertSingleNode.accept(resultSoup);
    assertCounts.accept(resultSoup.getResult().getNodes()[0], expectedSoupShardCount, expectedSoupObjectsCount);
  }
}
