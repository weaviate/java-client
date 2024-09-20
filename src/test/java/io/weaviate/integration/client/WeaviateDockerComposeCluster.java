package io.weaviate.integration.client;

import java.time.Duration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.weaviate.WeaviateContainer;

public class WeaviateDockerComposeCluster implements TestRule {

  public static class Weaviate extends WeaviateContainer {
    public Weaviate(String dockerImageName, String hostname, Boolean isJoining) {
      super(dockerImageName);
      withNetwork(Network.SHARED);
      withCreateContainerCmdModifier(cmd -> {
        cmd.withHostName(hostname);
      });
      withEnv("LOG_LEVEL", "debug");
      withEnv("CONTEXTIONARY_URL", "contextionary:9999");
      withEnv("QUERY_DEFAULTS_LIMIT", "25");
      withEnv("DEFAULT_VECTORIZER_MODULE", "text2vec-contextionary");
      withEnv("ENABLE_MODULES", "text2vec-contextionary,backup-filesystem,generative-openai");
      withEnv("BACKUP_FILESYSTEM_PATH", "/tmp/backups");
      withEnv("DISABLE_TELEMETRY", "true");
      withEnv("PERSISTENCE_FLUSH_IDLE_MEMTABLES_AFTER", "1");

      withEnv("CLUSTER_GOSSIP_BIND_PORT", "7110");
      withEnv("CLUSTER_DATA_BIND_PORT", "7111");
      withEnv("RAFT_PORT", "8300");

      withEnv("RAFT_BOOTSTRAP_EXPECT", "1");
      withEnv("RAFT_JOIN", "weaviate-0");
      if (isJoining) {
        withEnv("CLUSTER_JOIN", "weaviate-0:7110");
        waitingFor(Wait.forHttp("/v1/.well-known/ready").forPort(8080).forStatusCode(200).withStartupTimeout(Duration.ofSeconds(10)));
      }
    }
  }

  public static class Contextionary extends GenericContainer<WeaviateDockerCompose.Contextionary> {
    public Contextionary() {
      super("semitechnologies/contextionary:en0.16.0-v1.2.1");
      withNetwork(Network.SHARED);
      withCreateContainerCmdModifier(cmd -> cmd.withHostName("contextionary"));
      withEnv("OCCURRENCE_WEIGHT_LINEAR_FACTOR", "true");
      withEnv("PERSISTENCE_DATA_PATH", "/var/lib/weaviate");
      withEnv("OCCURRENCE_WEIGHT_LINEAR_FACTOR", "0.75");
      withEnv("EXTENSIONS_STORAGE_MODE", "weaviate");
      withEnv("EXTENSIONS_STORAGE_ORIGIN", "http://weaviate-0:8080");
      withEnv("NEIGHBOR_OCCURRENCE_IGNORE_PERCENTILE", "5");
      withEnv("ENABLE_COMPOUND_SPLITTING", "'false'");
    }
  }

  private static Contextionary contextionary;
  private static Weaviate weaviate0;
  private static Weaviate weaviate1;

  public void start() {
    contextionary = new Contextionary();
    contextionary.start();
    weaviate0 = new Weaviate(WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE, "weaviate-0", false);
    weaviate1 = new Weaviate(WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE, "weaviate-1", true);
    weaviate0.start();
    weaviate1.start();
  }

  public String getHttpHost0Address() {
    return weaviate0.getHttpHostAddress();
  }

  public String getGrpcHost0Address() {
    return weaviate0.getGrpcHostAddress();
  }

  public String getHttpHost1Address() {
    return weaviate1.getHttpHostAddress();
  }

  public String getGrpcHost1Address() {
    return weaviate1.getGrpcHostAddress();
  }

  public void stop() {
    weaviate0.stop();
    weaviate1.stop();
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
