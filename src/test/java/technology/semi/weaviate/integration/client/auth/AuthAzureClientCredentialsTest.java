package technology.semi.weaviate.integration.client.auth;

import java.io.File;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.auth.ClientCredentialsFlow;
import technology.semi.weaviate.client.v1.auth.exception.AuthException;
import technology.semi.weaviate.client.v1.misc.model.Meta;
import static technology.semi.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class AuthAzureClientCredentialsTest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-azure.yaml")
  ).withExposedService("weaviate-auth-azure_1", 8081, Wait.forHttp("/v1/.well-known/openid-configuration").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate-auth-azure_1", 8081);
    Integer port = compose.getServicePort("weaviate-auth-azure_1", 8081);
    address = host + ":" + port;
  }

  @Test
  public void testAuthAzure() throws AuthException {
    String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    if (StringUtils.isNotBlank(clientSecret)) {
      Config config = new Config("http", address);
      ClientCredentialsFlow clientCredentialsFlow = new ClientCredentialsFlow(clientSecret);
      WeaviateClient client = clientCredentialsFlow.getAuthClient(config, Arrays.asList("4706508f-30c2-469b-8b12-ad272b3de864/.default"));
      Result<Meta> meta = client.misc().metaGetter().run();
      assertNotNull(meta);
      assertNull(meta.getError());
      assertEquals("http://[::]:8081", meta.getResult().getHostname());
      assertEquals(EXPECTED_WEAVIATE_VERSION, meta.getResult().getVersion());
    } else {
      System.out.println("Skipping Azure Client Credentials test, missing AZURE_CLIENT_SECRET");
    }
  }

  @Test
  public void testAuthAzureHardcodedScope() throws AuthException {
    String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    if (StringUtils.isNotBlank(clientSecret)) {
      Config config = new Config("http", address);
      ClientCredentialsFlow clientCredentialsFlow = new ClientCredentialsFlow(clientSecret);
      WeaviateClient client = clientCredentialsFlow.getAuthClient(config, null);
      Result<Meta> meta = client.misc().metaGetter().run();
      assertNotNull(meta);
      assertNull(meta.getError());
      assertEquals("http://[::]:8081", meta.getResult().getHostname());
      assertEquals(EXPECTED_WEAVIATE_VERSION, meta.getResult().getVersion());
    } else {
      System.out.println("Skipping Azure Client Credentials test, missing AZURE_CLIENT_SECRET");
    }
  }
}
