package io.weaviate.integration;

import java.io.IOException;
import java.util.UUID;

import javax.net.ssl.TrustManagerFactory;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.containers.Container;
import io.weaviate.truststore.SingleTrustManagerFactory;
import io.weaviate.truststore.SpyTrustManager;

public class DefaultGrpcTransportITest extends ConcurrentTest {
  private static final WeaviateClient _setupClient = Container.WEAVIATE.getClient();

  private WeaviateClient client = Container.WEAVIATE.getClient();
  private TrustManagerFactory tmf;

  @Before
  public void setUp() throws IOException {
    tmf = SingleTrustManagerFactory.create(new SpyTrustManager());
    client = WeaviateClient.custom(opt -> opt
        .scheme("https")
        .httpHost(Container.WEAVIATE.getHost())
        .grpcHost(Container.WEAVIATE.getHost())
        .httpPort(Container.WEAVIATE.getMappedPort(8080))
        .grpcPort(Container.WEAVIATE.getMappedPort(50051))
        .trustManagerFactory(tmf));
  }

  @Test
  public void testCustomTrustStore_sync() throws IOException {
    // Arrange
    var nsThings = ns("Things");
    _setupClient.collections.create(nsThings);

    client.collections.use(nsThings).query.byId(UUID.randomUUID().toString());

    var spy = SpyTrustManager.getSpy(tmf);
    Assertions.assertThat(spy).get()
        .as("HttpClient uses custom TrustManager")
        .returns(true, SpyTrustManager::wasUsed);
  }

  @After
  public void tearDown() throws IOException {
    client.close();
  }
}
