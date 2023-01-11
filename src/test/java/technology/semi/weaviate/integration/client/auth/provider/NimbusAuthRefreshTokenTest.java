package technology.semi.weaviate.integration.client.auth.provider;

import java.io.File;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.auth.provider.AuthConfigUtil;
import technology.semi.weaviate.client.v1.auth.provider.AuthException;
import technology.semi.weaviate.client.v1.auth.provider.AuthType;
import technology.semi.weaviate.client.v1.auth.provider.NimbusAuth;
import technology.semi.weaviate.client.v1.misc.model.Meta;
import static technology.semi.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class NimbusAuthRefreshTokenTest {
  private String address;
  private Config refreshConfig;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-wcs.yaml")
  ).withExposedService("weaviate-auth-wcs_1", 8085, Wait.forHttp("/v1/.well-known/openid-configuration").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate-auth-wcs_1", 8085);
    Integer port = compose.getServicePort("weaviate-auth-wcs_1", 8085);
    address = host + ":" + port;
  }

  @Test
  public void testAuthWCS() throws AuthException, InterruptedException {
    class NimbusAuthAuthImpl extends NimbusAuth {
      public WeaviateClient getAuthClientWithOverriddenRefreshTokenValue(Config config, List<String> scopes,
        String username, String password) throws AuthException {
        return getAuthClient(config, "", username, password, scopes, AuthType.USER_PASSWORD);
      }

      @Override
      protected WeaviateClient getWeaviateClient(Config config, AuthResponse authResponse, String accessToken, long accessTokenLifeTime, String refreshToken) {
        // Here we override the getWeaviateClient just to get the auth config
        // and override the lifetime value so that the refresh token request appears faster
        // The lifetime of the access token is set artificially to 2 seconds
        refreshConfig = AuthConfigUtil.toAuthConfig(config, authResponse,
          accessToken, 2l, refreshToken);
        // here also we override the lifetime value to check that the client also works fine
        // with the refreshed token
        // The lifetime of the access token is set artificially to 2 seconds
        return super.getWeaviateClient(config, authResponse, accessToken, 2l, refreshToken);
      }
    }

    String password = System.getenv("WCS_DUMMY_CI_PW");
    if (StringUtils.isNotBlank(password)) {
      Config config = new Config("http", address);
      String username = "ms_2d0e007e7136de11d5f29fce7a53dae219a51458@existiert.net";
      assertThat(refreshConfig).isNull();
      NimbusAuthAuthImpl nimbusAuth = new NimbusAuthAuthImpl();
      WeaviateClient client = nimbusAuth.getAuthClientWithOverriddenRefreshTokenValue(config, null, username, password);
      assertThat(refreshConfig).isNotNull();
      // get the access token
      String firstBearerAccessTokenHeader = refreshConfig.getHeaders().get("Authorization");
      assertThat(firstBearerAccessTokenHeader).isNotBlank();
      Result<Meta> meta = client.misc().metaGetter().run();
      assertThat(meta).isNotNull();
      assertThat(meta.getError()).isNull();
      assertThat(meta.getResult().getHostname()).isEqualTo("http://[::]:8085");
      assertThat(meta.getResult().getVersion()).isEqualTo(EXPECTED_WEAVIATE_VERSION);
      Thread.sleep(3000l);
      // get the access token after refresh
      String afterRefreshBearerAccessTokenHeader = refreshConfig.getHeaders().get("Authorization");
      assertThat(firstBearerAccessTokenHeader).isNotEqualTo(afterRefreshBearerAccessTokenHeader);
      meta = client.misc().metaGetter().run();
      assertThat(meta).isNotNull();
      assertThat(meta.getError()).isNull();
      assertThat(meta.getResult().getHostname()).isEqualTo("http://[::]:8085");
      assertThat(meta.getResult().getVersion()).isEqualTo(EXPECTED_WEAVIATE_VERSION);
    } else {
      System.out.println("Skipping WCS Refresh Token test, missing WCS_DUMMY_CI_PW");
    }
  }
}
