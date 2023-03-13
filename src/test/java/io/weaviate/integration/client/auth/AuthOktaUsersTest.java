package io.weaviate.integration.client.auth;

import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.ResourceOwnerPasswordFlow;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.misc.model.Meta;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class AuthOktaUsersTest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-okta-users.yaml")
  ).withExposedService("weaviate-auth-okta-users_1", 8083, Wait.forHttp("/v1/.well-known/openid-configuration").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate-auth-okta-users_1", 8083);
    Integer port = compose.getServicePort("weaviate-auth-okta-users_1", 8083);
    address = host + ":" + port;
  }

  @Test
  public void testAuthOktaNoScope() throws AuthException {
    String password = System.getenv("OKTA_DUMMY_CI_PW");
    if (StringUtils.isNotBlank(password)) {
      Config config = new Config("http", address);
      String username = "test@test.de";
      ResourceOwnerPasswordFlow resourceOwnerPasswordFlow = new ResourceOwnerPasswordFlow(username, password);
      WeaviateClient client = resourceOwnerPasswordFlow.getAuthClient(config, null);
      Result<Meta> meta = client.misc().metaGetter().run();
      assertNotNull(meta);
      assertNull(meta.getError());
      assertEquals("http://[::]:8083", meta.getResult().getHostname());
      assertEquals(EXPECTED_WEAVIATE_VERSION, meta.getResult().getVersion());
    } else {
      System.out.println("Skipping Okta test, missing OKTA_DUMMY_CI_PW");
    }
  }
}
