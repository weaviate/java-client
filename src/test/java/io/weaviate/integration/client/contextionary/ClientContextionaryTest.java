package io.weaviate.integration.client.contextionary;

import java.io.File;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.contextionary.model.C11yWordsResponse;

public class ClientContextionaryTest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
          new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    address = host + ":" + port;
  }

  @Test
  public void testContextionaryGetter() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    // when
    Result<C11yWordsResponse> pizzaHawaii = client.c11y().conceptsGetter().withConcept("pizzaHawaii").run();
    // then
    Assert.assertNotNull(pizzaHawaii);
    Assert.assertNotNull(pizzaHawaii.getResult());
    Assert.assertNull(pizzaHawaii.getError());
  }

  @Test
  public void testContextionaryExtensionCreator() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    // when
    Result<Boolean> extensionSuccess = client.c11y().extensionCreator()
            .withConcept("xoxo").withDefinition("Hugs and kisses").withWeight(1.0f).run();
    // then
    Assert.assertNotNull(extensionSuccess);
    Assert.assertTrue(extensionSuccess.getResult());
  }
}
