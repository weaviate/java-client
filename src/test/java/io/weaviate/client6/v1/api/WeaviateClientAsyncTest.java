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
    // You might see a warning from gRPC saying that the channel has been
    // garbage-collected before it was closed. The stack trace will probably
    // show that it's related to this test.
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
