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
    // This test will fail if SOME Weaviate container is running on your machine
    // with default :8080 port exposed. All Testcontainer instances started by
    // the client's test suite expose random ports, which will not interferen with
    // this test.
    //
    // You might also see a warning from gRPC saying that the channel has been
    // garbage-collected before it was closed. The stack trace will probably
    // show that it's related to this test.
    WeaviateClient.connectToLocal();
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
