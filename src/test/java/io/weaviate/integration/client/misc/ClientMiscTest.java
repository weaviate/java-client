package io.weaviate.integration.client.misc;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.Meta;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.tests.misc.MiscTestSuite;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientMiscTest {

  private WeaviateClient client;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
  }

  @Test
  public void testMiscLivenessEndpoint() {
    // when
    Result<Boolean> livenessCheck = client.misc().liveChecker().run();
    // then
    MiscTestSuite.assertLivenessOrReadiness(livenessCheck);
  }

  @Test
  public void testMiscReadinessEndpoint() {
    // when
    Result<Boolean> readinessCheck = client.misc().readyChecker().run();
    // then
    MiscTestSuite.assertLivenessOrReadiness(readinessCheck);
  }

  @Test
  public void testMiscMetaEndpoint() {
    // when
    Result<Meta> meta = client.misc().metaGetter().run();
    // then
    MiscTestSuite.assertMeta(meta);
  }
}
