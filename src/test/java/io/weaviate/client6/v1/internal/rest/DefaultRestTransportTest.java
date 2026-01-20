package io.weaviate.client6.v1.internal.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.TrustManagerFactory;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

import io.weaviate.client6.v1.internal.Timeout;
import io.weaviate.testutil.truststore.SingleTrustManagerFactory;
import io.weaviate.testutil.truststore.SpyTrustManager;

public class DefaultRestTransportTest {
  private ClientAndServer mockServer;
  private DefaultRestTransport transport;
  private TrustManagerFactory tmf;

  @Before
  public void setUp() throws IOException {
    // MockServer does not verify exclusive ownership of the port
    // and using any well-known port like 8080 will produce flaky
    // test results with fairly confusing errors, like:
    //
    // path /mockserver/verifySequence was not found
    //
    // if another webserver is listening to that port.
    // We use 0 to let the underlying system find an available port.
    mockServer = ClientAndServer.startClientAndServer(0);
    mockServer.withSecure(true);

    tmf = SingleTrustManagerFactory.create(new SpyTrustManager());
    transport = new DefaultRestTransport(new RestTransportOptions(
        "https", "localhost", mockServer.getLocalPort(),
        Collections.emptyMap(), null, tmf, new Timeout()));
  }

  @Test
  public void testCustomTrustStore_sync() throws IOException {
    transport.performRequest(null, OptionalEndpoint.noBodyOptional(
        request -> "GET", request -> "/", request -> null, (code, response) -> null));

    mockServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/"));

    var spy = SpyTrustManager.getSpy(tmf);
    Assertions.assertThat(spy).get()
        .as("HttpClient uses custom TrustManager")
        .returns(true, SpyTrustManager::wasUsed);
  }

  @Test
  public void testCustomTrustStore_async() throws IOException, ExecutionException, InterruptedException {
    transport.performRequestAsync(null, OptionalEndpoint.noBodyOptional(
        request -> "GET", request -> "/", request -> null, (code, response) -> null)).get();

    mockServer.verify(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/v1/"));

    var spy = SpyTrustManager.getSpy(tmf);
    Assertions.assertThat(spy).get()
        .as("HttpClient uses custom TrustManager")
        .returns(true, SpyTrustManager::wasUsed);
  }

  @After
  public void tearDown() throws Exception {
    mockServer.stop();
    transport.close();
  }
}
