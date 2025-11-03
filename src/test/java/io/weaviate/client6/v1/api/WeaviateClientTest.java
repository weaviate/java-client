package io.weaviate.client6.v1.api;

import org.junit.Test;

public class WeaviateClientTest {

  @SuppressWarnings("resource")
  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection() {
    var config = new Config.Local();
    config.host("localhost").port(1234);
    new WeaviateClient(config.build());
  }

  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection_Local() {
    try (final var __ = WeaviateClient.connectToLocal()) {
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection_WeaviateCloud() {
    WeaviateClient.connectToWeaviateCloud("no-cluster.io", "no-key");
  }

  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection_Custom() {
    WeaviateClient.connectToCustom(conn -> conn.httpHost("localhost").httpPort(1234));
  }
}
