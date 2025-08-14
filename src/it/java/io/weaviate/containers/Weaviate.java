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
  private WeaviateClient clientInstance;

  public static final String VERSION = "1.29.1";
  public static final String DOCKER_IMAGE = "semitechnologies/weaviate";

  public WeaviateClient getClient() {
    return getClient(ObjectBuilder.identity());
  }

  /**
   * Get a client for the current Weaviate container.
   * As we aren't running tests in parallel at the moment,
   * this is not made thread-safe.
   */
  public WeaviateClient getClient(Function<Config.Custom, ObjectBuilder<Config>> fn) {
    // FIXME: control from containers?
    if (!isRunning()) {
      start();
    }
    if (clientInstance == null) {
      var customFn = ObjectBuilder.partial(fn,
          conn -> conn
              .scheme("http")
              .httpHost(getHost())
              .grpcHost(getHost())
              .httpPort(getMappedPort(8080))
              .grpcPort(getMappedPort(50051)));
      try {
        clientInstance = WeaviateClient.custom(customFn);
      } catch (Exception e) {
        throw new RuntimeException("create WeaviateClient for Weaviate container", e);
      }
    }
    return clientInstance;
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
    private boolean telemetry;

    private Map<String, String> environment = new HashMap<>();

    public Builder() {
      this.versionTag = VERSION;
      this.telemetry = false;
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

    public Builder withContextionaryUrl(String url) {
      addModules(Contextionary.MODULE);
      environment.put("CONTEXTIONARY_URL", url);
      return this;
    }

    public Builder withImageInference(String url, String module) {
      addModules(module);
      environment.put("IMAGE_INFERENCE_API", "http://" + url);
      return this;
    }

    public Builder enableTelemetry(boolean enable) {
      environment.put("DISABLE_TELEMETRY", Boolean.toString(!enable));
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
    try {
      clientInstance.close();
    } catch (IOException e) {
      // TODO: log error
    }
  }
}
