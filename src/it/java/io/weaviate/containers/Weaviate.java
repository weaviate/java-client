package io.weaviate.containers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.testcontainers.weaviate.WeaviateContainer;

import io.weaviate.client6.Config;
import io.weaviate.client6.WeaviateClient;

public class Weaviate extends WeaviateContainer {
  private static WeaviateClient clientInstance;

  public static final String VERSION = "1.29.0";
  public static final String DOCKER_IMAGE = "semitechnologies/weaviate";

  /**
   * Get a client for the current Weaviate container.
   * As we aren't running tests in parallel at the moment,
   * this is not made thread-safe.
   */
  public WeaviateClient getClient() {
    if (clientInstance == null) {
      var config = new Config("http", getHttpHostAddress(), getGrpcHostAddress());
      clientInstance = new WeaviateClient(config);
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
    private Set<String> enableModules;
    private String defaultVectorizerModule;
    private String contextionaryUrl;
    private boolean telemetry;

    public Builder() {
      this.versionTag = VERSION;
      this.enableModules = new HashSet<>();
      this.telemetry = false;
    }

    public Builder withVersion(String version) {
      this.versionTag = version;
      return this;
    }

    public Builder addModule(String module) {
      enableModules.add(module);
      return this;
    }

    public Builder withDefaultVectorizer(String module) {
      addModule(module);
      defaultVectorizerModule = module;
      return this;
    }

    public Builder withContextionaryUrl(String url) {
      contextionaryUrl = url;
      return this;
    }

    public Builder enableTelemetry() {
      telemetry = true;
      return this;
    }

    public Weaviate build() {
      var c = new Weaviate(DOCKER_IMAGE + ":" + versionTag);

      if (!enableModules.isEmpty()) {
        c.withEnv("ENABLE_MODULES", String.join(",", enableModules));
      }
      if (defaultVectorizerModule != null) {
        c.withEnv("DEFAULT_VECTORIZER_MODULE", defaultVectorizerModule);
      }
      if (contextionaryUrl != null) {
        c.withEnv("CONTEXTIONARY_URL", contextionaryUrl);
      }
      if (!telemetry) {
        c.withEnv("DISABLE_TELEMETRY", "true");
      }

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
    try {
      clientInstance.close();
    } catch (IOException e) {
      // TODO: log error
    }
  }
}
