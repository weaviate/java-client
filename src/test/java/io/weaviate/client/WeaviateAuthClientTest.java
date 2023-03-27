package io.weaviate.client;

import io.weaviate.client.v1.auth.exception.AuthException;
import org.apache.http.client.methods.HttpGet;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class WeaviateAuthClientTest {

  private static final int mockServerPort = 8899;
  private static final int oidcMockServerPort = 8999;
  private static final String OIDC_URL = "/v1/.well-known/openid-configuration";

  private static final ClientAndServer mockServer = startClientAndServer(mockServerPort);
  private static final ClientAndServer oidcMockServer = startClientAndServer(oidcMockServerPort);

  @AfterClass
  public static void after() {
    mockServer.stop();
    oidcMockServer.stop();
  }

  @Test
  public void test404Case() {
    //given
    String msg = "Auth001: The client was configured to use authentication, but weaviate is configured without authentication. Are you sure this is " +
      "correct?";

    mockServer.reset();
    new MockServerClient("localhost", mockServerPort)
      .when(
        request().withMethod("GET").withPath(OIDC_URL)
      )
      .respond(
        response().withStatusCode(404)
      );
    new MockServerClient("localhost", mockServerPort)
      .retrieveRecordedRequests(
        request().withMethod("GET").withPath(OIDC_URL)
      );
    //when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientCredentials(config, "some-secret", null);
    });
    AuthException exceptionClientPassword = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientPassword(config, "user", "pass", null);
    });
    AuthException exceptionBearerToken = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.bearerToken(config, "access-token", 0l, "refresh-token");
    });
    //then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertEquals(msg, exceptionBearerToken.getMessage());

    new MockServerClient("localhost", mockServerPort)
      .verify(
        request()
          .withPath(OIDC_URL),
        VerificationTimes.exactly(3)
      );
  }

  @Test
  public void test503Case() {
    //given
    int statusCode = 503;
    String msg = String.format("OIDC configuration url %s returned status code %s",
      String.format("http://localhost:%s%s", mockServerPort, OIDC_URL), statusCode);

    mockServer.reset();
    new MockServerClient("localhost", mockServerPort)
      .when(
        request().withMethod("GET").withPath(OIDC_URL)
      )
      .respond(
        response().withStatusCode(statusCode)
      );
    new MockServerClient("localhost", mockServerPort)
      .retrieveRecordedRequests(
        request().withMethod("GET").withPath(OIDC_URL)
      );
    //when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientCredentials(config, "some-secret", null);
    });
    AuthException exceptionClientPassword = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientPassword(config, "user", "pass", null);
    });
    AuthException exceptionBearerToken = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.bearerToken(config, "access-token", 0l, "refresh-token");
    });
    //then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertEquals(msg, exceptionBearerToken.getMessage());

    new MockServerClient("localhost", mockServerPort)
      .verify(
        request()
          .withPath(OIDC_URL),
        VerificationTimes.exactly(3)
      );
  }

  @Test
  public void test201OIDCHrefCase() {
    //given
    int statusCode = 201;
    String hrefURL = String.format("http://localhost:%s/oidc", oidcMockServerPort);
    String msg = String.format("OIDC configuration url %s returned status code %s",
      hrefURL, statusCode);

    mockServer.reset();
    new MockServerClient("localhost", mockServerPort)
      .when(
        request().withMethod("GET").withPath(OIDC_URL)
      )
      .respond(
        response().withStatusCode(200).withBody(String.format("{\"href\":\"%s\"}", hrefURL))
      );
    oidcMockServer.reset();
    new MockServerClient("localhost", oidcMockServerPort)
      .when(
        request().withMethod("GET").withPath("/oidc")
      )
      .respond(
        response().withStatusCode(statusCode)
      );
    //when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientCredentials(config, "some-secret", null);
    });
    AuthException exceptionClientPassword = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientPassword(config, "user", "pass", null);
    });
    AuthException exceptionBearerToken = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.bearerToken(config, "access-token", 0l, "refresh-token");
    });
    //then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertEquals(msg, exceptionBearerToken.getMessage());
  }

  @Test
  public void test200ParseException() throws AuthException {
    //given
    String hrefURL = String.format("http://localhost:%s/oidc", oidcMockServerPort);
    String msg = "Invalid JSON: Unexpected token parse-exception} at position 17.";

    mockServer.reset();
    new MockServerClient("localhost", mockServerPort)
      .when(
        request().withMethod("GET").withPath(OIDC_URL)
      )
      .respond(
        response().withStatusCode(200).withBody(String.format("{\"href\":\"%s\"}", hrefURL))
      );
    oidcMockServer.reset();
    new MockServerClient("localhost", oidcMockServerPort)
      .when(
        request().withMethod("GET").withPath("/oidc")
      )
      .respond(
        response().withStatusCode(200).withBody("{parse-exception}")
      );
    //when
    AuthException exceptionClientCredentials = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientCredentials(config, "some-secret", null);
    });
    AuthException exceptionClientPassword = assertThrows(AuthException.class, () -> {
      Config config = new Config("http", String.format("localhost:%s", mockServerPort));
      WeaviateAuthClient.clientPassword(config, "user", "pass", null);
    });
    Config config = new Config("http", String.format("localhost:%s", mockServerPort));
    WeaviateClient weaviateClient = WeaviateAuthClient.bearerToken(config, "access-token", 0l, "");
    //then
    assertEquals(msg, exceptionClientCredentials.getMessage());
    assertEquals(msg, exceptionClientPassword.getMessage());
    assertNotNull(weaviateClient);
  }

  @Test
  public void shouldAddApiKeyHeader() throws AuthException {
    String metaPath = "/v1/meta";
    String apiKey = "some-api-key";
    HttpRequest requestDefinition = request().withMethod(HttpGet.METHOD_NAME).withPath(metaPath);
    Config config = new Config("http", String.format("localhost:%s", mockServerPort));

    mockServer.reset();
    new MockServerClient("localhost", mockServerPort)
      .when(requestDefinition)
      .respond(response().withStatusCode(200));

    WeaviateAuthClient.apiKey(config, apiKey).misc().metaGetter().run();

    new MockServerClient("localhost", mockServerPort)
      .verify(
        request().withMethod(HttpGet.METHOD_NAME).withPath(metaPath)
          .withHeader("Authorization", String.format("Bearer %s", apiKey)),
        VerificationTimes.once()
      );
  }
}
