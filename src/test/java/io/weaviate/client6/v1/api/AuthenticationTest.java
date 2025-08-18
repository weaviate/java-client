package io.weaviate.client6.v1.api;

import java.io.IOException;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

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
            Collections.emptyMap(), null, null));
  }

  @Test
  public void testAuthentication_apiKey() throws Exception {
    var authz = Authentication.apiKey("my-api-key");
    var transportOptions = new RestTransportOptions(
        "http", "localhost", mockServer.getLocalPort(),
        Collections.emptyMap(), authz.getTokenProvider(noAuthTransport), null);

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

  @After
  public void stopMockServer() throws Exception {
    mockServer.stop();
    noAuthTransport.close();
  }
}
