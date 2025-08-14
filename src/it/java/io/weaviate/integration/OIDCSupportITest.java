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

public class OIDCSupportITest extends ConcurrentTest {
  private static final Weaviate wcsContainer = Weaviate.custom()
      .withOIDC("wcs", "https://auth.wcs.api.weaviate.io/auth/realms/SeMI", "email", "groups")
      .build();

  private static final String WCS_DUMMY_CI_USER = System.getenv("WCS_DUMMY_CI_USER");
  private static final String WCS_DUMMY_CI_PW = System.getenv("WCS_DUMMY_CI_PW");
  private static final String OKTA_CLIENT_SECRET = System.getenv("OKTA_CLIENT_SECRET");

  @Test
  public void test_resourceOwnerPassword() throws IOException {
    checkSkip();

    var authz = Authorization.resourceOwnerPassword(WCS_DUMMY_CI_USER, WCS_DUMMY_CI_PW, List.of("test_scope"));

    try (final var client = wcsContainer.getClient(conn -> conn.authorization(authz))) {
      Assertions.assertThat(client.isLive()).isTrue();
    }
  }

  @Test
  public void test_bearerToken() {
    checkSkip();
  }

  private static void checkSkip() {
    Assume.assumeTrue("WCS_DUMMY_CI_USER is not set", WCS_DUMMY_CI_USER != null);
    Assume.assumeTrue("WCS_DUMMY_CI_PW is not set", WCS_DUMMY_CI_PW != null);
    Assume.assumeTrue("OKTA_CLIENT_SECRET is not set", OKTA_CLIENT_SECRET != null);
    Assume.assumeTrue("no internet connection", ping("www.google.com"));
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
