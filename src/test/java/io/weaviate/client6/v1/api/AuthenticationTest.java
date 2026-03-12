package io.weaviate.client6.v1.api;

import java.io.IOException;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

import io.weaviate.client6.v1.internal.Timeout;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransportOptions;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public class AuthenticationTest {
  private ClientAndServer mockServer;
  private RestTransport noAuthTransport;

  @Before
  public void startMockServer() throws IOException {
    // MockServer does not verify exclusive ownership of the port
    // and using any well-known port like 8080 will produce flaky
    // test results with fairly confusing errors, like:
    //
    // path /mockserver/verifySequence was not found
    //
    // if another webserver is listening to that port.
    // We use 0 to let the underlying system find an available port.
    mockServer = ClientAndServer.startClientAndServer(0);
    noAuthTransport = new DefaultRestTransport(
        new RestTransportOptions(
            "http", "localhost", mockServer.getLocalPort(),
            Collections.emptyMap(), null, null, new Timeout()));
  }

  @Test
  public void testAuthentication_apiKey() throws Exception {
    var authz = Authentication.apiKey("my-api-key");
    var transportOptions = new RestTransportOptions(
        "http", "localhost", mockServer.getLocalPort(),
        Collections.emptyMap(), authz.getTokenProvider(noAuthTransport), null, new Timeout());

    try (final var restClient = new DefaultRestTransport(transportOptions)) {
      restClient.performRequest(null, SimpleEndpoint.sideEffect(
          request -> "GET", request -> "/", request -> null));
    } catch (WeaviateApiException ex) {
      if (ex.httpStatusCode() != 404) {
        Assertions.fail("unexpected error", ex);
      }
    }

    mockServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/")
            .withHeader("Authorization", "Bearer my-api-key"));
  }

  @Test
  public void testAuthentication_resourceOwnerPasswordWithClientSecret() throws Exception {
    // 1. Mock /.well-known/openid-configuration
    mockServer.when(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/.well-known/openid-configuration")
    ).respond(
        org.mockserver.model.HttpResponse.response()
            .withStatusCode(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"clientId\": \"my-client-id\", \"href\": \"http://localhost:" + mockServer.getLocalPort() + "/oidc-provider\"}")
    );

    // 2. Mock OIDC provider metadata
    mockServer.when(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/oidc-provider")
    ).respond(
        org.mockserver.model.HttpResponse.response()
            .withStatusCode(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"issuer\": \"http://localhost:" + mockServer.getLocalPort() + "\", \"token_endpoint\": \"http://localhost:" + mockServer.getLocalPort() + "/token\"}")
    );

    // 3. Mock Token Endpoint
    mockServer.when(
        HttpRequest.request()
            .withMethod("POST")
            .withPath("/token")
    ).respond(
        org.mockserver.model.HttpResponse.response()
            .withStatusCode(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\": \"secret-token\", \"token_type\": \"Bearer\", \"expires_in\": 3600}")
    );

    var authz = Authentication.resourceOwnerPasswordCredentials("my-client-secret", "my-user", "my-pass", Collections.emptyList());
    var transportOptions = new RestTransportOptions(
        "http", "localhost", mockServer.getLocalPort(),
        Collections.emptyMap(), authz.getTokenProvider(noAuthTransport), null, new Timeout());

    try (final var restClient = new DefaultRestTransport(transportOptions)) {
      restClient.performRequest(null, SimpleEndpoint.sideEffect(
          request -> "GET", request -> "/", request -> null));
    } catch (WeaviateApiException ex) {
      if (ex.httpStatusCode() != 404) {
        Assertions.fail("unexpected error", ex);
      }
    }

    // Verify token request had both password grant and client authentication
    mockServer.verify(
        HttpRequest.request()
            .withMethod("POST")
            .withPath("/token")
            .withBody(org.mockserver.model.ParameterBody.params(
                org.mockserver.model.Parameter.param("grant_type", "password"),
                org.mockserver.model.Parameter.param("username", "my-user"),
                org.mockserver.model.Parameter.param("password", "my-pass"),
                org.mockserver.model.Parameter.param("client_id", "my-client-id"),
                org.mockserver.model.Parameter.param("client_secret", "my-client-secret"),
                org.mockserver.model.Parameter.param("scope", "offline_access")
            ))
    );

    // Verify the actual request used the obtained token
    mockServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/")
            .withHeader("Authorization", "Bearer secret-token"));
  }

  @After
  public void stopMockServer() throws Exception {
    mockServer.stop();
    noAuthTransport.close();
  }
}
