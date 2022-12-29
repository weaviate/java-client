package technology.semi.weaviate.integration.client.misc;

import java.io.File;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.misc.model.Meta;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static technology.semi.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class ClientMiscTest {

  @ClassRule
  public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));
  private WeaviateClient client;

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    Config config = new Config("http", host + ":" + port);

    client = new WeaviateClient(config);
  }

  @Test
  public void testMiscLivenessEndpoint() {
    // when
    Result<Boolean> livenessCheck = client.misc().liveChecker().run();
    // then
    assertNotNull(livenessCheck);
    assertTrue(livenessCheck.getResult());
  }

  @Test
  public void testMiscReadinessEndpoint() {
    // when
    Result<Boolean> readinessCheck = client.misc().readyChecker().run();
    // then
    assertNotNull(readinessCheck);
    assertTrue(readinessCheck.getResult());
  }

  @Test
  public void testMiscMetaEndpoint() {
    // when
    Result<Meta> meta = client.misc().metaGetter().run();
    // then
    assertNotNull(meta);
    assertNull(meta.getError());
    assertEquals("http://[::]:8080", meta.getResult().getHostname());
    assertEquals(EXPECTED_WEAVIATE_VERSION, meta.getResult().getVersion());
    assertEquals("{backup-filesystem={backupsPath=/tmp/backups}, text2vec-contextionary={version=en0.16.0-v1.1.0, wordCount=818072.0}}", meta.getResult().getModules().toString());
  }
}
