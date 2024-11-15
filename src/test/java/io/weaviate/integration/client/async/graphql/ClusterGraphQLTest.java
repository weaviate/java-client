package io.weaviate.integration.client.async.graphql;

import com.google.gson.internal.LinkedTreeMap;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.graphql.GraphQL;
import io.weaviate.client.v1.async.graphql.api.Get;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.integration.client.WeaviateDockerComposeCluster;
import io.weaviate.integration.client.WeaviateTestGenerics;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(JParamsTestRunner.class)
public class ClusterGraphQLTest extends AbstractAsyncClientTest {
  private static final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
  private String address;

  private WeaviateClient syncClient;
  private WeaviateAsyncClient client;
  private GraphQL gql;

  @ClassRule
  public static WeaviateDockerComposeCluster compose = new WeaviateDockerComposeCluster();

  @Before
  public void before() {
    address = compose.getHttpHost0Address();

    syncClient = new WeaviateClient(new Config("http", address));
    testGenerics.createReplicatedTestSchemaAndData(syncClient);

    client = syncClient.async();
    gql = client.graphQL();
  }
  
  @After
  public void after() {
    testGenerics.cleanupWeaviate(syncClient);
    client.close();
  }
  
  public static Object[][] provideConsistencyLevels() {
    return new Object[][]{ { ConsistencyLevel.ALL }, { ConsistencyLevel.QUORUM }, { ConsistencyLevel.ONE } };
  }

  @DataMethod(source = ClusterGraphQLTest.class, method = "provideConsistencyLevels")
  @Test
  public void testGraphQLGetUsingConsistencyLevel(String consistency) {
    Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza").withConsistencyLevel(consistency)
      .withFields(field("name"), _additional("isConsistent")));

    List<LinkedTreeMap<String, LinkedTreeMap<String, Boolean>>> pizzas = extractClass(result, "Get", "Pizza");
    for (LinkedTreeMap<String, LinkedTreeMap<String, Boolean>> pizza : pizzas) {
      assertTrue("not consistent with ConsistencyLevel=" + consistency, pizza.get("_additional").get("isConsistent"));
    }
  }

  private Result<GraphQLResponse> doGet(Consumer<Get> build) {
    Get get = gql.get();
    build.accept(get);
    try {
      return get.run().get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.get(): " + e.getMessage());
      return null;
    }
  }
}
