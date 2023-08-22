package io.weaviate.integration.client.proxy;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.Meta;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;

public class ClientProxyTest {

  private WeaviateClient client;

  @ClassRule
  public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
    new File("src/test/resources/docker-compose-proxy.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200)
  ).withExposedService("proxy_1", 80, Wait.forHttp("/").forStatusCode(503));

  @Test
  public void testProxyUnset() {
    Config config = new Config("http", "weaviate.local");

    client = new WeaviateClient(config);
    // when
    Result<Meta> meta = client.misc().metaGetter().run();
    // then
    assertNotNull(meta);
    assertNotNull(meta.getError());
  }

  @Test
  public void testProxySet() {
    String proxyHost = compose.getServiceHost("proxy_1", 80);
    Integer port = compose.getServicePort("proxy_1", 80);
    String proxyScheme = "http";

    Config config = new Config("http", "weaviate.local");
    config.setProxy(proxyHost, port, proxyScheme);

    client = new WeaviateClient(config);
    // when
    Result<Meta> meta = client.misc().metaGetter().run();
    // then
    assertNotNull(meta);
    assertNull(meta.getError());
    assertEquals("http://[::]:8080", meta.getResult().getHostname());
  }
}
