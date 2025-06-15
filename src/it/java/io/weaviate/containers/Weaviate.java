package io.weaviate.containers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testcontainers.weaviate.WeaviateContainer;

import io.weaviate.client6.v1.api.Config;
import io.weaviate.client6.v1.api.WeaviateClient;

public class Weaviate extends WeaviateContainer {
  private WeaviateClient clientInstance;

  public static final String VERSION = "1.29.0";
  public static final String DOCKER_IMAGE = "semitechnologies/weaviate";

  /**
   * Get a client for the current Weaviate container.
   * As we aren't running tests in parallel at the moment,
   * this is not made thread-safe.
   */
  public WeaviateClient getClient() {
    // FIXME: control from containers?
    if (!isRunning()) {
      start();
    }
    if (clientInstance == null) {
      var config = new Config("http", getHttpHostAddress(), getGrpcHostAddress());
      try {
        clientInstance = new WeaviateClient(config);
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
      telemetry = enable;
      return this;
    }

    public Weaviate build() {
      var c = new Weaviate(DOCKER_IMAGE + ":" + versionTag);

      if (!enableModules.isEmpty()) {
        c.withEnv("ENABLE_API_BASED_MODULES", "'true'");
        c.withEnv("ENABLE_MODULES", String.join(",", enableModules));
      }
      if (!telemetry) {
        c.withEnv("DISABLE_TELEMETRY", "true");
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
