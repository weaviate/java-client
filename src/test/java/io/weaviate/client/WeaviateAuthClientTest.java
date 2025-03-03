package io.weaviate.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.apache.http.client.methods.HttpGet;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;

import io.weaviate.client.v1.auth.exception.AuthException;

public class WeaviateAuthClientTest {

  private static final String MOCK_SERVER_HOST = "localhost";
  private static final int MOCK_SERVER_PORT = 8899;
  private static final int OIDC_MOCK_SERVER_PORT = 8999;
  private static final Config MOCK_SERVER_CONFIG = new Config(
      "http",
      String.format("%s:%s", MOCK_SERVER_HOST, MOCK_SERVER_PORT));
  private static final String OIDC_PATH = "/v1/.well-known/openid-configuration";

  private static final ClientAndServer mockServer = startClientAndServer(MOCK_SERVER_PORT);
  private static final ClientAndServer oidcMockServer = startClientAndServer(OIDC_MOCK_SERVER_PORT);

  @AfterClass
  public static void after() {
    mockServer.stop();
    oidcMockServer.stop();
  }

  @Test
  public void test404Case() {
    // given
    String msg = "Auth001: The client was configured to use authentication, but weaviate is configured without authentication. Are you sure this is "
        +
        "correct?";

    mockServer.reset();
    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .when(
            request().withMethod("GET").withPath(OIDC_PATH))
        .respond(
            response().withStatusCode(404));
    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .retrieveRecordedRequests(
            request().withMethod("GET").withPath(OIDC_PATH));
    // when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientCredentials(MOCK_SERVER_CONFIG, "some-secret", null));
    AuthException exceptionClientPassword = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientPassword(MOCK_SERVER_CONFIG, "user", "pass", null));
    AuthException exceptionBearerToken = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.bearerToken(MOCK_SERVER_CONFIG, "access-token", 0l, "refresh-token"));
    // then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertEquals(msg, exceptionBearerToken.getMessage());

    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .verify(
            request()
                .withPath(OIDC_PATH),
            VerificationTimes.exactly(3));
  }

  @Test
  public void test503Case() {
    // given
    int statusCode = 503;
    String msg = String.format("OIDC configuration url %s returned status code %s",
        String.format("http://%s:%s%s", MOCK_SERVER_HOST, MOCK_SERVER_PORT, OIDC_PATH), statusCode);

    mockServer.reset();
    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .when(
            request().withMethod("GET").withPath(OIDC_PATH))
        .respond(
            response().withStatusCode(statusCode));
    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .retrieveRecordedRequests(
            request().withMethod("GET").withPath(OIDC_PATH));
    // when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientCredentials(MOCK_SERVER_CONFIG, "some-secret", null));
    AuthException exceptionClientPassword = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientPassword(MOCK_SERVER_CONFIG, "user", "pass", null));
    AuthException exceptionBearerToken = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.bearerToken(MOCK_SERVER_CONFIG, "access-token", 0l, "refresh-token"));
    // then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertEquals(msg, exceptionBearerToken.getMessage());

    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .verify(
            request()
                .withPath(OIDC_PATH),
            VerificationTimes.exactly(6));
  }

  @Test
  public void test201OIDCHrefCase() {
    // given
    int statusCode = 201;
    String hrefURL = String.format("http://%s:%s/oidc", MOCK_SERVER_HOST, OIDC_MOCK_SERVER_PORT);
    String msg = String.format("OIDC configuration url %s returned status code %s",
        hrefURL, statusCode);

    mockServer.reset();
    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .when(
            request().withMethod("GET").withPath(OIDC_PATH))
        .respond(
            response().withStatusCode(200).withBody(String.format("{\"href\":\"%s\"}", hrefURL)));
    oidcMockServer.reset();
    new MockServerClient(MOCK_SERVER_HOST, OIDC_MOCK_SERVER_PORT)
        .when(
            request().withMethod("GET").withPath("/oidc"))
        .respond(
            response().withStatusCode(statusCode));
    // when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientCredentials(MOCK_SERVER_CONFIG, "some-secret", null));
    AuthException exceptionClientPassword = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientPassword(MOCK_SERVER_CONFIG, "user", "pass", null));
    AuthException exceptionBearerToken = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.bearerToken(MOCK_SERVER_CONFIG, "access-token", 0l, "refresh-token"));
    // then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertEquals(msg, exceptionBearerToken.getMessage());
  }

  @Test
  public void test200ParseException() throws AuthException {
    // given
    String hrefURL = String.format("http://%s:%s/oidc", MOCK_SERVER_HOST, OIDC_MOCK_SERVER_PORT);
    String msg = "Invalid JSON";

    mockServer.reset();
    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .when(
            request().withMethod("GET").withPath(OIDC_PATH))
        .respond(
            response().withStatusCode(200).withBody(String.format("{\"href\":\"%s\"}", hrefURL)));
    oidcMockServer.reset();
    new MockServerClient(MOCK_SERVER_HOST, OIDC_MOCK_SERVER_PORT)
        .when(
            request().withMethod("GET").withPath("/oidc"))
        .respond(
            response().withStatusCode(200).withBody("{parse-exception}"));
    // when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientCredentials(MOCK_SERVER_CONFIG, "some-secret", null));
    AuthException exceptionClientPassword = assertThrows(AuthException.class,
        () -> WeaviateAuthClient.clientPassword(MOCK_SERVER_CONFIG, "user", "pass", null));
    WeaviateClient weaviateClient = WeaviateAuthClient.bearerToken(MOCK_SERVER_CONFIG, "access-token", 0l, "");
    // then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertNotNull(weaviateClient);
  }

  @Test
  public void shouldAddApiKeyHeader() throws AuthException {
    String metaPath = "/v1/meta";
    String apiKey = "some-api-key";
    HttpRequest requestDefinition = request().withMethod(HttpGet.METHOD_NAME).withPath(metaPath);

    mockServer.reset();
    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .when(requestDefinition)
        .respond(response().withStatusCode(200));

    WeaviateAuthClient.apiKey(MOCK_SERVER_CONFIG, apiKey).misc().metaGetter().run();

    new MockServerClient(MOCK_SERVER_HOST, MOCK_SERVER_PORT)
        .verify(
            request().withMethod(HttpGet.METHOD_NAME).withPath(metaPath)
                .withHeader("Authorization", String.format("Bearer %s", apiKey)),
            VerificationTimes.once());
  }
}
