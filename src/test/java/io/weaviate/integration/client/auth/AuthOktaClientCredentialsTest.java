package io.weaviate.integration.client.auth;

import java.util.Arrays;

import io.weaviate.integration.client.WeaviateDockerImage;
import io.weaviate.integration.client.WeaviateWithOktaCcContainer;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.ClientCredentialsFlow;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.misc.model.Meta;
import org.testcontainers.weaviate.WeaviateContainer;

import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class AuthOktaClientCredentialsTest {
  private String address;

  @ClassRule
  public static WeaviateContainer weaviate = new WeaviateWithOktaCcContainer(WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE);

  @Before
  public void before() {
    address = weaviate.getHttpHostAddress();
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
