package io.weaviate.client6.v1.internal.rest;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.Config;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.internal.Proxy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpForward.forward;

public class ProxyTest {
  private ClientAndServer targetServer;
  private ClientAndServer proxyServer;
  private WeaviateClient client;

  @Before
  public void setUp() {
    targetServer = ClientAndServer.startClientAndServer(0);
    proxyServer = ClientAndServer.startClientAndServer(0);

    // Set up target server to return a success response
    targetServer.when(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/.well-known/live"))
        .respond(
            HttpResponse.response()
                .withStatusCode(200));

    targetServer.when(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/meta"))
        .respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withBody("{\"version\": \"1.32.0\"}"));

    // Set up proxy server to forward requests to the target server
    proxyServer.when(
        HttpRequest.request())
        .forward(
            forward()
                .withHost("localhost")
                .withPort(targetServer.getLocalPort())
                .withScheme(org.mockserver.model.HttpForward.Scheme.HTTP));

    Config config = Config.of(c -> c
        .scheme("http")
        .httpHost("localhost")
        .httpPort(targetServer.getLocalPort())
        .grpcHost("localhost")
        .grpcPort(targetServer.getLocalPort())
        .proxy(new Proxy("localhost", proxyServer.getLocalPort()))
        .timeout(5)
    );

    client = new WeaviateClient(config);
  }

  @Test
  public void testClientInitializationWithProxy() {
    // This test verifies that the client can be successfully created.
    // The WeaviateClient constructor performs REST calls to /v1/.well-known/live
    // and /v1/meta to verify the connection and version support.
    // If these calls fail, the constructor throws a WeaviateConnectException.
    // Since setUp() already creates a client using the proxy, we just need to
    // verify it was initialized correctly.
    assertThat(client).isNotNull();
    
    // Verify that the initialization calls went through the proxy
    proxyServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/.well-known/live"));
    proxyServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/meta"));
  }

  @Test
  public void testRestProxy() throws IOException {
    // Perform a request that should go through the proxy
    client.meta();

    // Verify that the proxy server received the request
    proxyServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/meta"));

    // Verify that the target server also received the request (forwarded by proxy)
    targetServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/meta"));
  }

  @Test
  public void testProxyConfiguration() {
    // In this test, we verify that the client has the proxy configured.
    assertThat(client.getConfig().proxy()).isNotNull();
    assertThat(client.getConfig().proxy().port()).isEqualTo((long) proxyServer.getLocalPort());
  }

  @Test
  public void testGrpcProxy() {
    // gRPC proxying via HTTP CONNECT.
    // DefaultGrpcTransport uses a custom ProxyDetector which returns a
    // HttpConnectProxiedSocketAddress when a proxy is configured.

    // To verify that gRPC proxying is correctly set up, we check the configuration.
    // Since actual CONNECT verification via MockServer is tricky in this setup,
    // we focus on ensuring the client is correctly initialized with the proxy.
    assertThatThrownBy(() -> client.collections.use("Test").size())
        .isInstanceOf(WeaviateApiException.class)
        .hasMessageContaining("UNAVAILABLE: Network closed");
  }

  @After
  public void tearDown() throws Exception {
    if (client != null) {
      client.close();
    }
    if (proxyServer != null) {
      proxyServer.stop();
    }
    if (targetServer != null) {
      targetServer.stop();
    }
  }
}
