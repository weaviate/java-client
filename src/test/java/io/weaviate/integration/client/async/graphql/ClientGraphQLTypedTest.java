package io.weaviate.integration.client.async.graphql;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.graphql.model.GraphQLTypedResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.graphql.ClientGraphQLTypedTestSuite;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientGraphQLTypedTest {
  private WeaviateClient client;
  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
    testGenerics.createTestSchemaAndData(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testGraphQLGet() {
    Supplier<Result<GraphQLTypedResponse<ClientGraphQLTypedTestSuite.Pizzas>>> supplyPizza =() -> {
      try (WeaviateAsyncClient asyncClient = client.async()) {
        return asyncClient.graphQL().get()
          .withClassName("Pizza")
          .withFields(Field.builder().name("name").build(), Field.builder().name("description").build())
          .run(ClientGraphQLTypedTestSuite.Pizzas.class).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
    ClientGraphQLTypedTestSuite.testGraphQLGet(supplyPizza);
  }
}
