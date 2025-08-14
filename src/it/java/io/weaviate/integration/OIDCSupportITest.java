package io.weaviate.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.Authorization;
import io.weaviate.containers.Weaviate;

/**
 * Test that the client can use one of the supported authorization flows to
 * obtain a token from the OIDC provider and use it in a request to Weaviate.
 *
 * Running this test suite successfully requires talking to external services,
 * so tests will be skipped if the don't have internet. See
 * {@link #hasInternetConnection}.
 */
public class OIDCSupportITest extends ConcurrentTest {
  private static final String WCS_DUMMY_CI_USERNAME = "oidc-test-user@weaviate.io";
  private static final String WCS_DUMMY_CI_PW = System.getenv("WCS_DUMMY_CI_PW");

  /**
   * Weaviate conatiner that users WCS-backed OIDC provider.
   * Supports ResourceOwnerPassword and RefreshToken authentication flows.
   */
  private static final Weaviate wcsContainer = Weaviate.custom()
      .withOIDC("wcs", "https://auth.wcs.api.weaviate.io/auth/realms/SeMI", "email", "groups")
      .build();

  private static final String OKTA_CLIENT_ID = "0oa7e9ipdkVZRUcxo5d7";
  private static final String OKTA_CLIENT_SECRET = System.getenv("OKTA_CLIENT_SECRET");

  /**
   * Weaviate container that uses Okta's dummy OIDC provider.
   * Supports ClientCredentials flow.
   */
  private static final Weaviate oktaContainer = Weaviate.custom()
      .withOIDC(OKTA_CLIENT_ID, "https://dev-32300990.okta.com/oauth2/aus7e9kxbwYQB0eht5d7", "cid", "groups")
      .build();

  @Test
  public void test_bearerToken() {
    Assume.assumeTrue("WCS_DUMMY_CI_PW is not set", WCS_DUMMY_CI_PW != null);
    Assume.assumeTrue("no internet connection", hasInternetConnection());
  }

  @Test
  public void test_resourceOwnerPassword() throws IOException {
    Assume.assumeTrue("WCS_DUMMY_CI_PW is not set", WCS_DUMMY_CI_PW != null);
    Assume.assumeTrue("no internet connection", hasInternetConnection());

    var authz = Authorization.resourceOwnerPassword(WCS_DUMMY_CI_USERNAME, WCS_DUMMY_CI_PW, List.of());
    pingWeaviate(wcsContainer, authz);
  }

  @Test
  public void test_clientCredentials() throws IOException {
    Assume.assumeTrue("OKTA_CLIENT_SECRET is not set", OKTA_CLIENT_SECRET != null);
    Assume.assumeTrue("no internet connection", hasInternetConnection());

    var authz = Authorization.clientCredentials(OKTA_CLIENT_ID, OKTA_CLIENT_SECRET, List.of());
    pingWeaviate(oktaContainer, authz);
  }

  private static void pingWeaviate(final Weaviate container, Authorization authz) throws IOException {
    try (final var client = container.getClient(conn -> conn.authorization(authz))) {
      Assertions.assertThat(client.isLive()).isTrue();
    }
  }

  private static boolean hasInternetConnection() {
    return ping("www.google.com");
  }

  private static boolean ping(String site) {
    InetSocketAddress addr = new InetSocketAddress(site, 80);
    try (final var sock = new Socket()) {
      sock.connect(addr, 3000);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
