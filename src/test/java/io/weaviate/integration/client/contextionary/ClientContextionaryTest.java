package io.weaviate.integration.client.contextionary;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.contextionary.model.C11yWordsResponse;
import io.weaviate.integration.client.WeaviateDockerCompose;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientContextionaryTest {
  private String address;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();
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
