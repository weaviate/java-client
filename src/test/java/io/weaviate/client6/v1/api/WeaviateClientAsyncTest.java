package io.weaviate.client6.v1.api;

import org.junit.Test;

public class WeaviateClientAsyncTest {

  @SuppressWarnings("resource")
  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection() {
    var config = new Config.Local();
    config.host("localhost").port(1234);
    new WeaviateClientAsync(config.build());
  }

  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection_Local() {
    WeaviateClientAsync.connectToLocal(conn -> conn.port(1234));
  }

  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection_WeaviateCloud() {
    WeaviateClientAsync.connectToWeaviateCloud("no-cluster.io", "no-key");
  }

  @Test(expected = WeaviateConnectException.class)
  public void testFailedConnection_Custom() {
    WeaviateClient.connectToCustom(conn -> conn.httpHost("localhost").httpPort(1234));
  }
}
