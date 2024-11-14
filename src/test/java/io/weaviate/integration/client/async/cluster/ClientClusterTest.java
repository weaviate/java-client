package io.weaviate.integration.client.async.cluster;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.cluster.api.NodesStatusGetter;
import io.weaviate.client.v1.cluster.model.NodeStatusOutput;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.cluster.ClusterTestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientClusterTest {

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
  public void testClusterNodesEndpointWithoutDataWithOutputVerbose() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<NodesStatusResponse>> resultSupplier = createSupplier(
        asyncClient, nodesStatusGetter -> nodesStatusGetter
          .withOutput(NodeStatusOutput.VERBOSE)
      );

      ClusterTestSuite.testNoDataOutputVerbose(resultSupplier);
    }
  }

  @Test
  public void testClusterNodesEndpointWithDataWithOutputVerbose() throws InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<NodesStatusResponse>> resultSupplier = createSupplier(
        asyncClient, nodesStatusGetter -> nodesStatusGetter
          .withOutput(NodeStatusOutput.VERBOSE)
      );

      ClusterTestSuite.testDataOutputVerbose(resultSupplier, testGenerics, client);
    }
  }

  @Test
  public void shouldGetNodeStatusPerClassWithOutputVerbose() throws InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<NodesStatusResponse>> resultSupplierAll = createSupplier(
        asyncClient, nodesStatusGetter -> nodesStatusGetter
          .withOutput(NodeStatusOutput.VERBOSE)
      );
      Supplier<Result<NodesStatusResponse>> resultSupplierPizza = createSupplier(
        asyncClient, nodesStatusGetter -> nodesStatusGetter
          .withOutput(NodeStatusOutput.VERBOSE)
          .withClassName("Pizza")
      );
      Supplier<Result<NodesStatusResponse>> resultSupplierSoup = createSupplier(
        asyncClient, nodesStatusGetter -> nodesStatusGetter
          .withOutput(NodeStatusOutput.VERBOSE)
          .withClassName("Soup")
      );

      ClusterTestSuite.testDataPerClassOutputVerbose(resultSupplierAll, resultSupplierPizza, resultSupplierSoup,
        testGenerics, client);
    }
  }

  @Test
  public void testClusterNodesEndpointWithOutputMinimalImplicit() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<NodesStatusResponse>> resultSupplier = createSupplier(
        asyncClient, nodesStatusGetter -> {}
      );

      ClusterTestSuite.testNoDataOutputMinimalImplicit(resultSupplier);
    }
  }

  private Supplier<Result<NodesStatusResponse>> createSupplier(WeaviateAsyncClient asyncClient,
                                                               Consumer<NodesStatusGetter> configure) {
    return () -> {
      try {
        NodesStatusGetter nodesStatusGetter = asyncClient.cluster().nodesStatusGetter();
        configure.accept(nodesStatusGetter);
        return nodesStatusGetter.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }
}
