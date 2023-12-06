package io.weaviate.integration.client;

import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class WeaviateContainer {

  public static class DockerContainer {
    private final GenericContainer<?> container;

    private DockerContainer(GenericContainer<?> container) {
      this.container = container;
    }

    public void start() {
      container.start();
    }

    public Integer getMappedPort(int originalPort) {
      return container.getMappedPort(originalPort);
    }

    public void stop() {
      container.stop();
    }
  }

  public static DockerContainer create(String image) {
    Map<String, String> env = new HashMap<>();
    env.put("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "true");
    env.put("QUERY_DEFAULTS_LIMIT", "20");
    env.put("PERSISTENCE_DATA_PATH", "./data");
    env.put("DEFAULT_VECTORIZER_MODULE", "none");
    GenericContainer<?> weaviate = new GenericContainer<>(image)
      .withEnv(env)
      .withExposedPorts(8080, 50051)
      .waitingFor(Wait.forListeningPorts(8080, 50051));
    return new DockerContainer(weaviate);
  }
}
