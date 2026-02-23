package io.weaviate.containers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.weaviate.WeaviateContainer;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.Config;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.VersionSupport.SemanticVersion;

public class Weaviate extends WeaviateContainer {
  public static final String DOCKER_IMAGE = "semitechnologies/weaviate";
  public static final String LATEST_VERSION = Version.latest().semver.toString();
  public static final String VERSION;

  static {
    VERSION = System.getenv().getOrDefault("WEAVIATE_VERSION", LATEST_VERSION);
  }
  public static String OIDC_ISSUER = "https://auth.wcs.api.weaviate.io/auth/realms/SeMI";

  private volatile SharedClient clientInstance;
  private final String containerName;

  public enum Version {
    V132(1, 32, 24),
    V133(1, 33, 11),
    V134(1, 34, 7),
    V135(1, 35, 2),
    V136(1, 36, "0-rc.0");

    public final SemanticVersion semver;

    private Version(int major, int minor, int patch) {
      this.semver = new SemanticVersion(major, minor, patch);
    }

    private Version(int major, int minor, String patch) {
      this.semver = new SemanticVersion(major, minor, patch);
    }

    public void orSkip() {
      ConcurrentTest.requireAtLeast(this);
    }

    public static Version latest() {
      Version[] versions = Version.class.getEnumConstants();
      if (versions == null) {
        throw new IllegalStateException("No versions are defined");
      }
      return versions[versions.length - 1];
    }
  }

  /**
   * By default, testcontainer's name is only available after calling
   * {@link #start}.
   * We need to know each container's name in advance to run a cluster
   * of several nodes, in which case we alse set the name manually.
   *
   * @see Builder#build()
   */
  @Override
  public String getContainerName() {
    return containerName != null
        ? containerName
        : super.getContainerName();
  }

  /**
   * Create a new instance of WeaviateClient connected to this container if none
   * exist. Get an existing client otherwise.
   *
   * The lifetime of this client is tied to that of its container, which means
   * that you do not need to {@code close} it manually. It will only truly close
   * after the parent Testcontainer is stopped.
   */
  public WeaviateClient getClient() {
    if (!isRunning()) {
      start();
    }
    if (clientInstance != null) {
      return clientInstance;
    }

    synchronized (this) {
      if (clientInstance == null) {
        try {
          clientInstance = new SharedClient(Config.of(defaultConfigFn()), this);
        } catch (Exception e) {
          throw new RuntimeException("create WeaviateClient for Weaviate container", e);
        }
      }
    }
    return clientInstance;
  }

  /**
   * Get client that is not shared with other tests / callers.
   * The returned client is not wrapped in an instance of {@link SharedClient},
   * so it can be auto-closed by the try-with-resources statement when it exists.
   */
  public WeaviateClient getBareClient() {
    if (!isRunning()) {
      start();
    }
    try {
      return new WeaviateClient(Config.of(defaultConfigFn()));
    } catch (Exception e) {
      throw new RuntimeException("create WeaviateClient for Weaviate container", e);
    }
  }

  /**
   * Create a new instance of WeaviateClient connected to this container.
   * Prefer using {@link #getClient} unless your test needs the initialization
   * steps to run, e.g. OIDC authorization grant exchange.
   */
  public WeaviateClient getClient(Function<Config.Custom, ObjectBuilder<Config>> fn) {
    if (!isRunning()) {
      start();
    }

    var customFn = ObjectBuilder.partial(fn, defaultConfigFn());
    var config = customFn.apply(new Config.Custom()).build();
    try {
      return new WeaviateClient(config);
    } catch (Exception e) {
      throw new RuntimeException("create WeaviateClient for Weaviate container", e);
    }
  }

  private Function<Config.Custom, ObjectBuilder<Config>> defaultConfigFn() {
    var host = getHost();
    return conn -> conn
        .scheme("http")
        .httpHost(host).httpPort(getMappedPort(8080))
        .grpcHost(host).grpcPort(getMappedPort(50051));
  }

  public static Weaviate createDefault() {
    return new Builder().build();
  }

  public static Weaviate.Builder custom() {
    return new Builder();
  }

  public static class Builder {
    private String versionTag = VERSION;
    private String containerName = "weaviate";
    private Set<String> enableModules = new HashSet<>();
    private Set<String> adminUsers = new HashSet<>();
    private Set<String> viewerUsers = new HashSet<>();
    private Map<String, String> environment = new HashMap<>();

    public Builder() {
      addModules(Reranker.Kind.DUMMY.jsonValue(), Generative.Kind.DUMMY.jsonValue());
      enableAutoSchema(false);
    }

    public Builder withContainerName(String containerName) {
      this.containerName = containerName;
      return this;
    }

    public Builder withVersion(String version) {
      this.versionTag = version;
      return this;
    }

    public Builder addModules(String... modules) {
      enableModules.addAll(Arrays.asList(modules));
      return this;
    }

    public Builder withDefaultVectorizer(String module) {
      addModules(module);
      environment.put("DEFAULT_VECTORIZER_MODULE", module);
      return this;
    }

    public Builder withModel2VecUrl(String url) {
      addModules(Model2Vec.MODULE);
      environment.put("MODEL2VEC_INFERENCE_API", "http://" + url);
      return this;
    }

    public Builder withImageInference(String url, String module) {
      addModules(module);
      environment.put("IMAGE_INFERENCE_API", "http://" + url);
      return this;
    }

    public Builder withOffloadS3(String accessKey, String secretKey) {
      addModules("offload-s3");
      environment.put("OFFLOAD_S3_ENDPOINT", "http://" + MinIo.URL);
      environment.put("OFFLOAD_S3_BUCKET_AUTO_CREATE", "true");
      environment.put("AWS_ACCESS_KEY_ID", accessKey);
      environment.put("AWS_SECRET_KEY", secretKey);
      return this;
    }

    public Builder withFilesystemBackup(String fsPath) {
      addModules("backup-filesystem");
      environment.put("BACKUP_FILESYSTEM_PATH", fsPath);
      return this;
    }

    public Builder withAdminUsers(String... admins) {
      adminUsers.addAll(Arrays.asList(admins));
      return this;
    }

    public Builder withViewerUsers(String... viewers) {
      viewerUsers.addAll(Arrays.asList(viewers));
      return this;
    }

    /** Enable RBAC authorization for this container. */
    public Builder withRbac() {
      environment.put("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
      environment.put("AUTHENTICATION_APIKEY_ENABLED", "true");
      environment.put("AUTHORIZATION_RBAC_ENABLED", "true");
      environment.put("AUTHENTICATION_DB_USERS_ENABLED", "true");
      return this;
    }

    /**
     * Enable API-Key authentication for this container.
     *
     * @param apiKeys Allowed API keys.
     */
    public Builder withApiKeys(String... apiKeys) {
      environment.put("AUTHENTICATION_APIKEY_ENABLED", "true");
      environment.put("AUTHENTICATION_APIKEY_ALLOWED_KEYS", String.join(",",
          apiKeys));
      return this;
    }

    public Builder withGrpcMaxMessageSize(int bytes) {
      environment.put("GRPC_MAX_MESSAGE_SIZE", String.valueOf(bytes));
      return this;
    }

    public Builder enableTelemetry(boolean enable) {
      environment.put("DISABLE_TELEMETRY", Boolean.toString(!enable));
      return this;
    }

    public Builder enableAutoSchema(boolean enable) {
      environment.put("AUTOSCHEMA_ENABLED", Boolean.toString(!enable));
      return this;
    }

    public Builder enableAnonymousAccess(boolean enable) {
      environment.put("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", Boolean.toString(enable));
      return this;
    }

    /** User default OIDC provider for integration tests. */
    public Builder withOIDC() {
      return withOIDC(
          "Peuc12y02UA0eAED1dqSjE5HtGUrpBsx",
          "https://auth.weaviate.cloud/Peuc12y02UA0eAED1dqSjE5HtGUrpBsx",
          "email", "roles");

    }

    public Builder withOIDC(String clientId, String issuer, String usernameClaim, String groupsClaim) {
      enableAnonymousAccess(false);
      environment.put("AUTHENTICATION_OIDC_ENABLED", "true");
      environment.put("AUTHENTICATION_OIDC_CLIENT_ID", clientId);
      environment.put("AUTHENTICATION_OIDC_ISSUER", issuer);
      environment.put("AUTHENTICATION_OIDC_USERNAME_CLAIM", usernameClaim);
      environment.put("AUTHENTICATION_OIDC_GROUPS_CLAIM", groupsClaim);
      return this;
    }

    public Weaviate build() {
      var c = new Weaviate(containerName, DOCKER_IMAGE + ":" + versionTag);

      if (!enableModules.isEmpty()) {
        c.withEnv("ENABLE_API_BASED_MODULES", Boolean.TRUE.toString());
        c.withEnv("ENABLE_MODULES", String.join(",", enableModules));
      }

      var apiKeyUsers = new HashSet<String>();
      apiKeyUsers.addAll(adminUsers);
      apiKeyUsers.addAll(viewerUsers);

      if (!adminUsers.isEmpty()) {
        environment.put("AUTHORIZATION_ADMIN_USERS", String.join(",", adminUsers));
      }
      if (!viewerUsers.isEmpty()) {
        environment.put("AUTHORIZATION_VIEWER_USERS", String.join(",", viewerUsers));
      }
      if (!apiKeyUsers.isEmpty()) {
        environment.put("AUTHENTICATION_APIKEY_USERS", String.join(",", apiKeyUsers));
      }

      environment.forEach((name, value) -> c.withEnv(name, value));
      c.withCreateContainerCmdModifier(cmd -> cmd.withHostName(containerName));
      return c;
    }
  }

  private Weaviate() {
    this("weaviate", DOCKER_IMAGE + ":" + VERSION);
  }

  private Weaviate(String containerName, String dockerImageName) {
    super(dockerImageName);
    this.containerName = containerName;
  }

  @Override
  public void stop() {
    // Note: at the moment containers which are not created as a @TestRule
    // will not be "stopped", so client's resources are also not being freed.
    // This is fine in tests, but may produce warnings about the gRPC channel
    // not shut down properly.
    super.stop();
    if (clientInstance == null) {
      return;
    }
    synchronized (this) {
      try {
        clientInstance.close(this);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** SharedClient's lifetime is tied to that of it's parent container. */
  private class SharedClient extends WeaviateClient {
    private final Weaviate parent;

    private SharedClient(Config config, Weaviate parent) {
      super(config);
      this.parent = parent;
    }

    private void close(Weaviate caller) throws Exception {
      if (caller == parent) {
        super.close();
      }
    }

    @Override
    public void close() throws IOException {
    }
  }

  public static Weaviate cluster(int replicas) {
    List<Weaviate> nodes = new ArrayList<>();
    for (var i = 0; i < replicas; i++) {
      nodes.add(Weaviate.custom()
          .withContainerName("weaviate-" + i)
          .build());
    }
    return new Cluster(nodes);
  }

  public static class Cluster extends Weaviate {
    /** Leader and followers combined. */
    private final List<Weaviate> nodes;

    private final Weaviate leader;
    private final List<Weaviate> followers;

    private Cluster(List<Weaviate> nodes) {
      assert nodes.size() > 1 : "cluster must have 1+ nodes";

      this.nodes = List.copyOf(nodes);
      this.leader = nodes.remove(0);
      this.followers = List.copyOf(nodes);

      for (var follower : followers) {
        follower.dependsOn(leader);
      }
      setNetwork(Network.SHARED);
      bindNodes(7110, 7111, 8300);
    }

    @Override
    public WeaviateContainer dependsOn(List<? extends Startable> startables) {
      leader.dependsOn(startables);
      return this;
    }

    @Override
    public void setNetwork(Network network) {
      nodes.forEach(node -> node.setNetwork(network));
    }

    @Override
    public WeaviateClient getClient() {
      if (!isRunning()) {
        start();
      }
      return leader.getClient();
    }

    @Override
    public void start() {
      followers.forEach(Startable::start); // testcontainers will resolve dependencies
    }

    @Override
    public void stop() {
      followers.forEach(Startable::stop);
      leader.stop();
    }

    /**
     * Set environment variables for inter-cluster communication.
     *
     * @param gossip Gossip bind port.
     * @param data   Data bind port.
     * @param raft   RAFT port.
     */
    private void bindNodes(int gossip, int data, int raft) {
      var publicPort = leader.getExposedPorts().get(0); // see WeaviateContainer Testcontainer.

      nodes.forEach(node -> node
          .withEnv("CLUSTER_GOSSIP_BIND_PORT", String.valueOf(gossip))
          .withEnv("CLUSTER_DATA_BIND_PORT", String.valueOf(data))
          .withEnv("REPLICA_MOVEMENT_ENABLED", "true")
          .withEnv("RAFT_PORT", String.valueOf(raft))
          .withEnv("RAFT_BOOTSTRAP_EXPECT", "1"));

      followers.forEach(node -> node
          .withEnv("CLUSTER_JOIN", leader.containerName + ":" + gossip)
          .withEnv("RAFT_JOIN", leader.containerName)
          .waitingFor(
              Wait.forHttp("/v1/.well-known/ready")
                  .forPort(publicPort)
                  .forStatusCode(200)
                  .withStartupTimeout(Duration.ofSeconds(10))));
    }
  }
}
