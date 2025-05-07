package io.weaviate.containers;

import org.testcontainers.containers.GenericContainer;

public class Multi2VecClip extends GenericContainer<Multi2VecClip> {
  public static final String DOCKER_IMAGE = "cr.weaviate.io/semitechnologies/multi2vec-clip";
  public static final String VERSION = "sentence-transformers-clip-ViT-B-32";

  public static final String MODULE = "multi2vec-clip";
  public static final String HOST_NAME = MODULE;
  public static final String URL = HOST_NAME + ":8080";

  static Multi2VecClip createDefault() {
    return new Builder().build();
  }

  static Multi2VecClip.Builder custom() {
    return new Builder();
  }

  public static class Builder {
    private String versionTag;

    public Builder() {
      this.versionTag = VERSION;
    }

    public Multi2VecClip build() {
      var container = new Multi2VecClip(DOCKER_IMAGE + ":" + versionTag);
      container
          .withEnv("ENABLE_CUDA", "'false'");
      container.withCreateContainerCmdModifier(cmd -> cmd.withHostName(HOST_NAME));
      return container;
    }
  }

  public Multi2VecClip(String image) {
    super(image);
  }
}
