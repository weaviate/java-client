package io.weaviate.integration.client.cluster;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.cluster.model.NodeStatusOutput;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.cluster.ClusterMultiTenancyTestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.function.Supplier;

public class ClientClusterMultiTenancyTest {

  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldGetNodeStatusPerClass() throws InterruptedException {
    Supplier<Result<NodesStatusResponse>> resultSupplierAll = () -> client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .run();
    Supplier<Result<NodesStatusResponse>> resultSupplierPizza = () -> client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .withClassName("Pizza")
      .run();
    Supplier<Result<NodesStatusResponse>> resultSupplierSoup = () -> client.cluster().nodesStatusGetter()
      .withOutput(NodeStatusOutput.VERBOSE)
      .withClassName("Soup")
      .run();

    ClusterMultiTenancyTestSuite.testMultiTenancyDataPerClassOutputVerbose(resultSupplierAll, resultSupplierPizza, resultSupplierSoup,
      testGenerics, client);
  }
}
