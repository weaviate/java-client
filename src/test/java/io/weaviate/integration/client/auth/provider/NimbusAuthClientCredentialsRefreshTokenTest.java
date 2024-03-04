package io.weaviate.integration.client.auth.provider;

import io.weaviate.integration.client.WeaviateDockerImage;
import io.weaviate.integration.client.WeaviateVersion;
import java.util.Arrays;
import java.util.List;

import io.weaviate.integration.client.WeaviateWithOktaCcContainer;
import org.apache.commons.lang3.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.auth.nimbus.AuthType;
import io.weaviate.client.v1.auth.nimbus.BaseAuth;
import io.weaviate.client.v1.auth.nimbus.NimbusAuth;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.auth.provider.AuthClientCredentialsTokenProvider;
import io.weaviate.client.v1.misc.model.Meta;
import org.testcontainers.weaviate.WeaviateContainer;

public class NimbusAuthClientCredentialsRefreshTokenTest {
  private String address;
  private AccessTokenProvider tokenProvider;

  @ClassRule
  public static WeaviateContainer weaviate = new WeaviateWithOktaCcContainer(WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE);

  @Before
  public void before() {
    address = weaviate.getHttpHostAddress();
  }

  @Test
  public void testAuthOkta() throws AuthException, InterruptedException {
    class NimbusAuthAuthImpl extends NimbusAuth {
      @Override
      protected AccessTokenProvider getTokenProvider(Config config, BaseAuth.AuthResponse authResponse, List<String> clientScopes,
        String accessToken, long accessTokenLifeTime, String refreshToken, String clientSecret, AuthType authType) {
        // Client Credentials flow
        tokenProvider = new AuthClientCredentialsTokenProvider(config, authResponse, clientScopes, accessToken, 2l, clientSecret);
        return tokenProvider;
      }
    }

    String clientSecret = System.getenv("OKTA_CLIENT_SECRET");
    if (StringUtils.isNotBlank(clientSecret)) {
      Config config = new Config("http", address);
      assertThat(tokenProvider).isNull();
      NimbusAuthAuthImpl nimbusAuth = new NimbusAuthAuthImpl();
      AccessTokenProvider provider = nimbusAuth.getAccessTokenProvider(config, clientSecret, "", "", Arrays.asList("some_scope"), AuthType.CLIENT_CREDENTIALS);
      WeaviateClient client = new WeaviateClient(config, provider);
      assertThat(tokenProvider).isNotNull();
      // get the access token
      String firstBearerAccessTokenHeader = tokenProvider.getAccessToken();
      assertThat(firstBearerAccessTokenHeader).isNotBlank();
      Result<Meta> meta = client.misc().metaGetter().run();
      assertThat(meta).isNotNull();
      assertThat(meta.getError()).isNull();
      assertThat(meta.getResult().getHostname()).isEqualTo("http://[::]:8082");
      assertThat(meta.getResult().getVersion()).isEqualTo(WeaviateVersion.EXPECTED_WEAVIATE_VERSION);
      Thread.sleep(3000l);
      // get the access token after refresh
      String afterRefreshBearerAccessTokenHeader = tokenProvider.getAccessToken();
      assertThat(firstBearerAccessTokenHeader).isNotEqualTo(afterRefreshBearerAccessTokenHeader);
      meta = client.misc().metaGetter().run();
      assertThat(meta).isNotNull();
      assertThat(meta.getError()).isNull();
      assertThat(meta.getResult().getHostname()).isEqualTo("http://[::]:8082");
      assertThat(meta.getResult().getVersion()).isEqualTo(WeaviateVersion.EXPECTED_WEAVIATE_VERSION);
    } else {
      System.out.println("Skipping Okta Client Credentials refresh token test, missing OKTA_CLIENT_SECRET");
    }
  }
}
