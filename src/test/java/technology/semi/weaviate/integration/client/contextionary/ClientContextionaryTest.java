package technology.semi.weaviate.integration.client.contextionary;

import java.io.File;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.contextionary.model.C11yWordsResponse;

public class ClientContextionaryTest {
  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));
  private String address;

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
