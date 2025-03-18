package io.weaviate.containers;

import org.testcontainers.containers.GenericContainer;

public class Contextionary extends GenericContainer<Contextionary> {
  public static final String VERSION = "en0.16.0-v1.2.1";
  public static final String DOCKER_IMAGE = "semitechnologies/contextionary";
  public static final String MODULE = "text2vec-contextionary";

  public static final String HOST_NAME = "contextionary";
  public static final String URL = HOST_NAME + ":9999";

  static Contextionary createDefault() {
    return new Builder().build();
  }

  static Contextionary.Builder custom() {
    return new Builder();
  }

  public static class Builder {
    private String versionTag;

    public Builder() {
      this.versionTag = VERSION;
    }

    public Contextionary build() {
      var container = new Contextionary(DOCKER_IMAGE + ":" + versionTag);
      container
          .withEnv("OCCURRENCE_WEIGHT_LINEAR_FACTOR", "true")
          .withEnv("PERSISTENCE_DATA_PATH", "/var/lib/weaviate")
          .withEnv("OCCURRENCE_WEIGHT_LINEAR_FACTOR", "0.75")
          .withEnv("EXTENSIONS_STORAGE_MODE", "weaviate")
          .withEnv("EXTENSIONS_STORAGE_ORIGIN", "http://weaviate:8080")
          .withEnv("NEIGHBOR_OCCURRENCE_IGNORE_PERCENTILE", "5")
          .withEnv("ENABLE_COMPOUND_SPLITTING", "'false'");
      container.withCreateContainerCmdModifier(cmd -> cmd.withHostName("contextionary"));
      return container;
    }
  }

  public Contextionary(String image) {
    super(image);
  }
}
