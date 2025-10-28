package io.weaviate.containers;

import org.testcontainers.containers.GenericContainer;

import io.weaviate.client6.v1.api.collections.VectorConfig;

public class Transformers extends GenericContainer<Transformers> {
  public static final String VERSION = "sentence-transformers-all-MiniLM-L6-v2";
  public static final String DOCKER_IMAGE = "cr.weaviate.io/semitechnologies/transformers-inference";
  public static final String MODULE = VectorConfig.Kind.TEXT2VEC_TRANSFORMERS.jsonValue();

  public static final String HOST_NAME = "transformers";
  public static final String URL = HOST_NAME + ":8080";

  static Transformers createDefault() {
    return new Builder().build();
  }

  static Transformers.Builder custom() {
    return new Builder();
  }

  public static class Builder {
    private String versionTag;

    public Builder() {
      this.versionTag = VERSION;
    }

    public Transformers build() {
      var container = new Transformers(DOCKER_IMAGE + ":" + versionTag);
      container
          .withEnv("ENABLE_CUDA", "0");
      container.withCreateContainerCmdModifier(cmd -> cmd.withHostName(HOST_NAME));
      return container;
    }
  }

  public Transformers(String image) {
    super(image);
  }
}
