package io.weaviate.integration.client;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.weaviate.WeaviateContainer;

public class WeaviateDockerCompose implements TestRule {

  private final String weaviateVersion;

  public WeaviateDockerCompose() {
    this.weaviateVersion = WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE;
  }

  public WeaviateDockerCompose(String version) {
    this.weaviateVersion = String.format("semitechnologies/weaviate:%s", version);
  }

  public class Weaviate extends WeaviateContainer {
    public Weaviate(String dockerImageName, Network network) {
      super(dockerImageName);
      waitingFor(Wait.forHttp("/v1/.well-known/ready").forPort(8080).forStatusCode(200));
      withNetwork(network);
      withEnv("LOG_LEVEL", "debug");
      withEnv("CONTEXTIONARY_URL", "contextionary:9999");
      withEnv("QUERY_DEFAULTS_LIMIT", "25");
      withEnv("DEFAULT_VECTORIZER_MODULE", "text2vec-contextionary");
      withEnv("ENABLE_MODULES", "text2vec-contextionary,backup-filesystem,generative-openai");
      withEnv("BACKUP_FILESYSTEM_PATH", "/tmp/backups");
      withEnv("DISABLE_TELEMETRY", "true");
      withEnv("PERSISTENCE_FLUSH_IDLE_MEMTABLES_AFTER", "1");
      withCreateContainerCmdModifier(cmd -> cmd.withHostName("weaviate"));
    }
  }

  public class Contextionary extends GenericContainer<Contextionary> {
    public Contextionary(Network network) {
      super("semitechnologies/contextionary:en0.16.0-v1.2.1");
      withNetwork(network);
      withEnv("OCCURRENCE_WEIGHT_LINEAR_FACTOR", "true");
      withEnv("PERSISTENCE_DATA_PATH", "/var/lib/weaviate");
      withEnv("OCCURRENCE_WEIGHT_LINEAR_FACTOR", "0.75");
      withEnv("EXTENSIONS_STORAGE_MODE", "weaviate");
      withEnv("EXTENSIONS_STORAGE_ORIGIN", "http://weaviate:8080");
      withEnv("NEIGHBOR_OCCURRENCE_IGNORE_PERCENTILE", "5");
      withEnv("ENABLE_COMPOUND_SPLITTING", "'false'");
      withCreateContainerCmdModifier(cmd -> cmd.withHostName("contextionary"));
    }
  }

  private static Contextionary contextionary;
  private static Weaviate weaviate;

  public void start() {
    try (Network network = Network.newNetwork()) {
      contextionary = new Contextionary(network);
      contextionary.start();
      weaviate = new Weaviate(this.weaviateVersion, network);
      weaviate.start();
    }
  }

  public String getHttpHostAddress() {
    return weaviate.getHttpHostAddress();
  }

  public String getGrpcHostAddress() {
    return weaviate.getGrpcHostAddress();
  }

  public void stop() {
    weaviate.stop();
    contextionary.stop();
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          start();
          base.evaluate();
        } finally {
          stop();
        }
      }
    };
  }
}
