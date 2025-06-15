package io.weaviate.integration;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.Authorization;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.RestTransportOptions;

public class AuthorizationITest extends ConcurrentTest {
  private ClientAndServer mockServer;

  @Before
  public void startMockServer() {
    mockServer = ClientAndServer.startClientAndServer(8080);
  }

  @Test
  public void testAuthorization_apiKey() throws IOException {
    var transportOptions = new RestTransportOptions(
        "http", "localhost", 8080,
        Collections.emptyMap(), Authorization.apiKey("my-api-key"));

    try (final var restClient = new DefaultRestTransport(transportOptions)) {
      restClient.performRequest(null, Endpoint.of(
          request -> "GET",
          request -> "/",
          (gson, request) -> null,
          request -> null,
          code -> code != 200,
          (gson, response) -> null));
    }

    mockServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/")
            .withHeader("Authorization", "Bearer my-api-key"));
  }

  @After
  public void stopMockServer() {
    mockServer.stop();
  }
}
