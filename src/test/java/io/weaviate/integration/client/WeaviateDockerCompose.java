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

public class WeaviateDockerCompose implements TestRule {

  /** Weaviate Docker image to create a container from. */
  private final String weaviateVersion;
  private final boolean withOffloadS3;

  /** Username of the admin user for instances using RBAC. */
  private final String adminUser;

  public WeaviateDockerCompose() {
    this.weaviateVersion = WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE;
    this.withOffloadS3 = false;
    this.adminUser = null;
  }

  public WeaviateDockerCompose(String version) {
    this.weaviateVersion = String.format("semitechnologies/weaviate:%s", version);
    this.withOffloadS3 = false;
    this.adminUser = null;
  }

  public WeaviateDockerCompose(String version, boolean withOffloadS3) {
    this.weaviateVersion = String.format("semitechnologies/weaviate:%s", version);
    this.withOffloadS3 = withOffloadS3;
    this.adminUser = null;
  }

  public WeaviateDockerCompose(String version, String adminUser) {
    this.weaviateVersion = WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE;
    this.withOffloadS3 = false;
    this.adminUser = adminUser;
  }

  /** Create docker-compose deployment with auth and RBAC-authz enabled. */
  public static WeaviateDockerCompose rbac(String adminUser) {
    return new WeaviateDockerCompose(WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE, adminUser);
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
    }

    /** Create Weaviate container with RBAC authz and an admin user. */
    public Weaviate(String dockerImageName, boolean withOffloadS3, String adminUser) {
      this(dockerImageName, withOffloadS3);
      withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
      withEnv("AUTHENTICATION_APIKEY_ENABLED", "true");
      withEnv("AUTHORIZATION_RBAC_ENABLED", "true");
      withEnv("AUTHENTICATION_APIKEY_USERS", adminUser);
      withEnv("AUTHENTICATION_APIKEY_ALLOWED_KEYS", makeSecret(adminUser));
      withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
      withEnv("AUTHORIZATION_ADMIN_USERS", adminUser);
    }

    /**
     * Generate API secret for a username. When running an instance with
     * authentication enabled, {@link Weaviate} will use this method to generate
     * secrets for all users.
     * Use this method to get a valid API key for a test client.
     */
    public static String makeSecret(String user) {
      return user + "-secret";
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
    if (adminUser == null) {
      weaviate = new Weaviate(this.weaviateVersion, this.withOffloadS3);
    } else {
      weaviate = new Weaviate(this.weaviateVersion, this.withOffloadS3, this.adminUser);
    }
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
