package io.weaviate.integration.client.auth;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.misc.model.Meta;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthWCSUsersApiKeyTest {

  private Config config;
  private static final String API_KEY = "my-secret-key";
  private static final String INVALID_API_KEY = "my-not-so-secret-key";

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-wcs.yaml")
  ).withExposedService("weaviate-auth-wcs_1", 8085, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate-auth-wcs_1", 8085);
    Integer port = compose.getServicePort("weaviate-auth-wcs_1", 8085);
    config = new Config("http", host + ":" + port);
  }

  @Test
  public void shouldAuthenticateWithValidApiKey() throws AuthException {
    WeaviateClient client = WeaviateAuthClient.apiKey(config, API_KEY);
    Result<Meta> meta = client.misc().metaGetter().run();

    assertThat(meta).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .returns("http://[::]:8085", Meta::getHostname)
      .returns(EXPECTED_WEAVIATE_VERSION, Meta::getVersion);
  }

  @Test
  public void shouldNotAuthenticateWithInvalidApiKey() throws AuthException {
    WeaviateClient client = WeaviateAuthClient.apiKey(config, INVALID_API_KEY);
    Result<Meta> meta = client.misc().metaGetter().run();

    assertThat(meta).isNotNull()
      .returns(true, Result::hasErrors)
      .returns(null, Result::getResult)
      .extracting(Result::getError)
      .returns(401, WeaviateError::getStatusCode);
  }
}
