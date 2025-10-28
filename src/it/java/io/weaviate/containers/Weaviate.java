package io.weaviate.containers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.testcontainers.weaviate.WeaviateContainer;

import io.weaviate.client6.v1.api.Config;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public class Weaviate extends WeaviateContainer {
  public static final String VERSION = "1.33.0";
  public static final String DOCKER_IMAGE = "semitechnologies/weaviate";
  public static String OIDC_ISSUER = "https://auth.wcs.api.weaviate.io/auth/realms/SeMI";

  private volatile SharedClient clientInstance;

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
    private String versionTag;
    private Set<String> enableModules = new HashSet<>();
    private Set<String> adminUsers = new HashSet<>();
    private Set<String> viewerUsers = new HashSet<>();
    private Map<String, String> environment = new HashMap<>();

    public Builder() {
      this.versionTag = VERSION;
      enableAutoSchema(false);
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

    public Builder withTransformersUrl(String url) {
      addModules(Transformers.MODULE);
      environment.put("TRANSFORMERS_INFERENCE_API", "http://" + url);
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
      var c = new Weaviate(DOCKER_IMAGE + ":" + versionTag);

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
      c.withCreateContainerCmdModifier(cmd -> cmd.withHostName("weaviate"));
      return c;
    }
  }

  private Weaviate(String dockerImageName) {
    super(dockerImageName);
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
}
