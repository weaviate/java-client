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
  public void testFailedConnection_Local() throws Exception {
    // You might see a warning from gRPC saying that the channel has been
    // garbage-collected before it was closed. The stack trace will probably
    // show that it's related to this test.
    WeaviateClient.connectToLocal(conn -> conn.port(1234));
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
