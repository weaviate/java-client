package technology.semi.weaviate.integration.client.auth;

import java.io.File;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateAuthClient;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.auth.ClientCredentialsFlow;
import technology.semi.weaviate.client.v1.auth.ResourceOwnerPasswordFlow;
import technology.semi.weaviate.client.v1.auth.provider.AuthException;
import technology.semi.weaviate.client.v1.misc.model.Meta;
import static technology.semi.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class AuthOktaClientCredentialsTest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-okta-cc.yaml")
  ).withExposedService("weaviate-auth-okta-cc_1", 8082, Wait.forHttp("/v1/.well-known/openid-configuration").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate-auth-okta-cc_1", 8082);
    Integer port = compose.getServicePort("weaviate-auth-okta-cc_1", 8082);
    address = host + ":" + port;
  }

  @Test
  public void testAuthOkta() throws AuthException {
    String clientSecret = System.getenv("OKTA_CLIENT_SECRET");
    if (StringUtils.isNotBlank(clientSecret)) {
      Config config = new Config("http", address);
      ClientCredentialsFlow clientCredentialsFlow = new ClientCredentialsFlow(clientSecret);
      WeaviateClient client = clientCredentialsFlow.getAuthClient(config, Arrays.asList("some_scope"));
      Result<Meta> meta = client.misc().metaGetter().run();
      assertNotNull(meta);
      assertNull(meta.getError());
      assertEquals("http://[::]:8082", meta.getResult().getHostname());
      assertEquals(EXPECTED_WEAVIATE_VERSION, meta.getResult().getVersion());
    } else {
      System.out.println("Skipping Okta Client Credentials test, missing OKTA_CLIENT_SECRET");
    }
  }
}
