package io.weaviate.containers;

import org.testcontainers.containers.GenericContainer;

import io.weaviate.client6.v1.api.collections.VectorConfig;

public class Model2Vec extends GenericContainer<Model2Vec> {
  public static final String VERSION = "minishlab-potion-base-4M";
  public static final String DOCKER_IMAGE = "cr.weaviate.io/semitechnologies/model2vec-inference";
  public static final String MODULE = VectorConfig.Kind.TEXT2VEC_MODEL2VEC.jsonValue();

  public static final String HOST_NAME = "model2vec";
  public static final String URL = HOST_NAME + ":8080";

  static Model2Vec createDefault() {
    return new Builder().build();
  }

  static Model2Vec.Builder custom() {
    return new Builder();
  }

  public static class Builder {
    private String versionTag;

    public Builder() {
      this.versionTag = VERSION;
    }

    public Model2Vec build() {
      var container = new Model2Vec(DOCKER_IMAGE + ":" + versionTag);
      container.withCreateContainerCmdModifier(cmd -> cmd.withHostName(HOST_NAME));
      return container;
    }
  }

  public Model2Vec(String image) {
    super(image);
  }
}
