package io.weaviate.containers;

import org.testcontainers.containers.GenericContainer;

public class Img2VecNeural extends GenericContainer<Img2VecNeural> {
  public static final String DOCKER_IMAGE = "cr.weaviate.io/semitechnologies/img2vec-pytorch";
  public static final String VERSION = "resnet50";

  public static final String MODULE = "img2vec-neural";
  public static final String HOST_NAME = MODULE;
  public static final String URL = HOST_NAME + ":8080";

  static Img2VecNeural createDefault() {
    return new Builder().build();
  }

  static Img2VecNeural.Builder custom() {
    return new Builder();
  }

  public static class Builder {
    private String versionTag;

    public Builder() {
      this.versionTag = VERSION;
    }

    public Img2VecNeural build() {
      var container = new Img2VecNeural(DOCKER_IMAGE + ":" + versionTag);
      container.withCreateContainerCmdModifier(cmd -> cmd.withHostName(HOST_NAME));
      return container;
    }
  }

  public Img2VecNeural(String image) {
    super(image);
  }
}
