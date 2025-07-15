package io.weaviate.integration.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.weaviate.WeaviateContainer;

public class WeaviateDockerComposeBackup implements TestRule {

  public static final String ADMIN_KEY = "admin-key";

  /** Weaviate Docker image to create a container from. */
  private final String weaviateVersion;
  private final boolean withOffloadS3;

  public WeaviateDockerComposeBackup() {
    this.weaviateVersion = WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE;
    this.withOffloadS3 = false;
  }

  public WeaviateDockerComposeBackup(String version) {
    this.weaviateVersion = String.format("semitechnologies/weaviate:%s", version);
    this.withOffloadS3 = false;
  }

  public WeaviateDockerComposeBackup(String version, boolean withOffloadS3) {
    this.weaviateVersion = String.format("semitechnologies/weaviate:%s", version);
    this.withOffloadS3 = withOffloadS3;
  }

  public static class Weaviate extends WeaviateContainer {
    public Weaviate(String dockerImageName, boolean withOffloadS3) {
      super(dockerImageName);
      waitingFor(Wait.forHttp("/v1/.well-known/ready").forPort(8080).forStatusCode(200));
      withNetwork(Network.SHARED);
      List<String> enableModules = new ArrayList<>();
      enableModules.add("text2vec-contextionary");
      enableModules.add("backup-filesystem");
      enableModules.add("generative-openai");
      if (withOffloadS3) {
        enableModules.add("offload-s3");
        withEnv("OFFLOAD_S3_ENDPOINT", "http://minio:9000");
        withEnv("OFFLOAD_S3_BUCKET_AUTO_CREATE", "true");
        withEnv("AWS_ACCESS_KEY_ID", MinIO.USER);
        withEnv("AWS_SECRET_KEY", MinIO.PASSWORD);
      }
      withEnv("LOG_LEVEL", "debug");
      withEnv("CONTEXTIONARY_URL", "contextionary:9999");
      withEnv("QUERY_DEFAULTS_LIMIT", "25");
      withEnv("DEFAULT_VECTORIZER_MODULE", "text2vec-contextionary");
      withEnv("BACKUP_FILESYSTEM_PATH", "/tmp/backups");
      withEnv("DISABLE_TELEMETRY", "true");
      withEnv("PERSISTENCE_FLUSH_IDLE_MEMTABLES_AFTER", "1");
      withEnv("ENABLE_MODULES", String.join(",", enableModules));
      withCreateContainerCmdModifier(cmd -> cmd.withHostName("weaviate"));

      withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
      withEnv("AUTHENTICATION_APIKEY_ENABLED", "true");
      withEnv("AUTHENTICATION_APIKEY_ALLOWED_KEYS", ADMIN_KEY + ",custom-key");
      withEnv("AUTHENTICATION_APIKEY_USERS", "admin-user,custom-user");
      withEnv("AUTHORIZATION_ADMIN_USERS", "admin-user");
      withEnv("PERSISTENCE_DATA_PATH", "./data-weaviate-0");
      withEnv("CLUSTER_IN_LOCALHOST", "true");
      withEnv("CLUSTER_GOSSIP_BIND_PORT", "7100");
      withEnv("CLUSTER_DATA_BIND_PORT", "7101");
      withEnv("RAFT_BOOTSTRAP_EXPECT", "1");
      withEnv("AUTHORIZATION_ENABLE_RBAC", "true");
      withEnv("AUTHENTICATION_DB_USERS_ENABLED", "true");
      withEnv("AUTHENTICATION_OIDC_ENABLED", "true");
      withEnv("AUTHENTICATION_OIDC_CLIENT_ID", "wcs");
      withEnv("AUTHENTICATION_OIDC_ISSUER", "https://auth.wcs.api.weaviate.io/auth/realms/SeMI");
      withEnv("AUTHENTICATION_OIDC_USERNAME_CLAIM", "email");
      withEnv("AUTHENTICATION_OIDC_GROUPS_CLAIM", "groups");
    }
  }

  public static class Contextionary extends GenericContainer<Contextionary> {
    public Contextionary() {
      super("semitechnologies/contextionary:en0.16.0-v1.2.1");
      withNetwork(Network.SHARED);
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

  public static class MinIO extends MinIOContainer {
    private static final String USER = "minioadmin";
    private static final String PASSWORD = "minioadmin";

    public MinIO() {
      super("minio/minio");
      withNetwork(Network.SHARED);
      withUserName(USER);
      withPassword(PASSWORD);
      withCreateContainerCmdModifier(cmd -> cmd.withHostName("minio"));
    }
  }

  private static Contextionary contextionary;
  private static Weaviate weaviate;
  private static MinIO minio;

  public void start() {
    if (withOffloadS3) {
      minio = new MinIO();
      minio.start();
    }
    contextionary = new Contextionary();
    contextionary.start();
    weaviate = new Weaviate(this.weaviateVersion, this.withOffloadS3);
    weaviate.start();
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
    if (withOffloadS3) {
      minio.stop();
    }
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
