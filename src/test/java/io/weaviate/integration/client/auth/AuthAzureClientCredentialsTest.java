package io.weaviate.integration.client.auth;

import java.util.Arrays;

import io.weaviate.integration.client.WeaviateWithAzureContainer;
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

public class AuthAzureClientCredentialsTest {
  private String address;

  @ClassRule
  public static WeaviateContainer weaviate = new WeaviateWithAzureContainer("semitechnologies/weaviate:1.23.1");

  @Before
  public void before() {
    address = weaviate.getHttpHostAddress();
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
