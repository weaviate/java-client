package io.weaviate.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.Authentication;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.rest.RestTransport;
import io.weaviate.containers.Weaviate;

/**
 * Test that the client can use one of the supported authentication flows to
 * obtain a token from the OIDC provider and use it in a request to Weaviate.
 *
 * Running this test suite successfully requires talking to external services,
 * so tests will be skipped if we don't have internet. See
 * {@link #hasInternetConnection}.
 * Additionally, {@code WCS_DUMMY_CI_PW} and {@code OKTA_CLIENT_SECRET}
 * environment variables must be set.
 */
public class OIDCSupportITest extends ConcurrentTest {
  private static final String WCS_DUMMY_CI_USERNAME = "oidc-test-user@weaviate.io";
  private static final String WCS_DUMMY_CI_PW = System.getenv("WCS_DUMMY_CI_PW");

  /**
   * Weaviate container that uses WCS-backed OIDC provider.
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

  /**
   * Exchange a Resource Owner Password grant for a bearer token
   * and authenticate with it.
   */
  @Test
  public void test_bearerToken() throws Exception {
    Assume.assumeTrue("WCS_DUMMY_CI_PW is not set", WCS_DUMMY_CI_PW != null);
    Assume.assumeTrue("no internet connection", hasInternetConnection());

    var passwordAuth = Authentication.resourceOwnerPassword(WCS_DUMMY_CI_USERNAME, WCS_DUMMY_CI_PW, List.of());
    var t = SpyTokenProvider.stealToken(passwordAuth);
    Assertions.assertThat(t.isValid()).as("bearer token is valid").isTrue();

    // Expire this token immediately to force the client to fetch a new one.
    var auth = SpyTokenProvider.spyOn(Authentication.bearerToken(t.accessToken(), t.refreshToken(), 0));
    pingWeaviate(wcsContainer, auth);

    var newT = auth.getToken();
    Assertions.assertThat(newT.accessToken())
        .as("expect access_token was refreshed")
        .isNotEqualTo(t.accessToken());

    // Check that the new token authenticates requests.
    pingWeaviate(wcsContainer, auth);
    pingWeaviateAsync(wcsContainer, auth);
  }

  @Test
  public void test_resourceOwnerPassword() throws Exception {
    Assume.assumeTrue("WCS_DUMMY_CI_PW is not set", WCS_DUMMY_CI_PW != null);
    Assume.assumeTrue("no internet connection", hasInternetConnection());

    // Check norwal resource owner password flow works.
    var password = Authentication.resourceOwnerPassword(WCS_DUMMY_CI_USERNAME, WCS_DUMMY_CI_PW, List.of());
    var auth = SpyTokenProvider.spyOn(password);
    pingWeaviate(wcsContainer, auth);
    pingWeaviateAsync(wcsContainer, auth);

    // Get the token obtained by the wrapped TokenProvider.
    var t = auth.getToken();

    // Now make all tokens expire immediately, forcing the client to refresh..
    // Verify the new token is different from the one before.
    auth.setExpiresIn(0);
    pingWeaviate(wcsContainer, auth);

    var newT = auth.getToken();
    Assertions.assertThat(newT.accessToken())
        .as("expect access_token was refreshed")
        .isNotEqualTo(t.accessToken());
  }

  @Test
  public void test_clientCredentials() throws Exception {
    Assume.assumeTrue("OKTA_CLIENT_SECRET is not set", OKTA_CLIENT_SECRET != null);
    Assume.assumeTrue("no internet connection", hasInternetConnection());

    // Check norwal client credentials flow works.
    var cc = Authentication.clientCredentials(OKTA_CLIENT_ID, OKTA_CLIENT_SECRET, List.of());
    var auth = SpyTokenProvider.spyOn(cc);
    pingWeaviate(oktaContainer, auth);
    pingWeaviateAsync(oktaContainer, auth);

    // Get the token obtained by the wrapped TokenProvider.
    var t = auth.getToken();

    // Now make all tokens expire immediately, forcing the client to refresh..
    // Verify the new token is different from the one before.
    auth.setExpiresIn(0);
    pingWeaviate(oktaContainer, auth);

    var newT = auth.getToken();
    Assertions.assertThat(newT.accessToken())
        .as("expect access_token was refreshed")
        .isNotEqualTo(t.accessToken());
  }

  /** Send an HTTP and gRPC requests using a "sync" client. */
  private static void pingWeaviate(final Weaviate container, Authentication auth) throws Exception {
    try (final var client = container.getClient(conn -> conn.authentication(auth))) {
      // Make an authenticated HTTP call
      Assertions.assertThat(client.isLive()).isTrue();

      // Make an authenticated gRPC call
      var nsThings = unique("Things");
      client.collections.create(nsThings);
      var things = client.collections.use(nsThings);
      var randomUuid = UUID.randomUUID().toString();
      Assertions.assertThat(things.data.exists(randomUuid)).isFalse();
    }
  }

  /** Send an HTTP and gRPC requests using an "async" client. */
  private static void pingWeaviateAsync(final Weaviate container, Authentication auth) throws Exception {
    try (final var client = container.getClient(conn -> conn.authentication(auth))) {
      try (final var async = client.async()) {
        // Make an authenticated HTTP call
        Assertions.assertThat(async.isLive().join()).isTrue();

        // Make an authenticated gRPC call
        var nsThings = unique("Things");
        async.collections.create(nsThings).join();
        var things = async.collections.use(nsThings);
        var randomUuid = UUID.randomUUID().toString();
        Assertions.assertThat(things.data.exists(randomUuid).join()).isFalse();
      }
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

  /**
   * SpyTokenProvider is an Authentication implementation that spies on the
   * TokenProvider it creates and can expose tokens generated by it.
   */
  private static class SpyTokenProvider implements Authentication, TokenProvider {

    /** Spy on the TokenProvider returned by thie Authentication. */
    static SpyTokenProvider spyOn(Authentication auth) {
      return new SpyTokenProvider(auth);
    }

    /** Spy a token obtained by another TokenProvider. */
    static Token stealToken(Authentication auth) throws Exception {
      var spy = spyOn(auth);
      pingWeaviate(wcsContainer, spy);
      return spy.getToken();
    }

    private Long expiresIn;
    private Authentication authentication;
    private TokenProvider tokenProvider;

    private SpyTokenProvider(Authentication actual) {
      this.authentication = actual;
    }

    @Override
    public TokenProvider getTokenProvider(RestTransport transport) {
      tokenProvider = authentication.getTokenProvider(transport);
      return this;
    }

    @Override
    public Token getToken() {
      var t = tokenProvider.getToken();
      if (expiresIn != null) {
        t = Token.expireAfter(t.accessToken(), t.refreshToken(), expiresIn);
      }
      return t;
    }

    /** Expire all tokens in {@code expiresIn} seconds. */
    void setExpiresIn(long expiresIn) {
      this.expiresIn = expiresIn;
    }

  }
}
