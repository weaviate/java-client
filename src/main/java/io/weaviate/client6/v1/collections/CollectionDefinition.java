package io.weaviate.client6.v1.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.weaviate.client6.v1.collections.Vectors.NamedVectors;

public record CollectionDefinition(String name, List<Property> properties, Vectors vectors) {

  public static CollectionDefinition with(String name, Consumer<Configuration> options) {
    var config = new Configuration(options);
    return new CollectionDefinition(name, config.properties, config.vectors);
  }

  // Tucked Builder for additional collection configuration.
  public static class Configuration {
    public List<Property> properties = new ArrayList<>();
    public Vectors vectors;

    public Configuration properties(Property... properties) {
      this.properties = Arrays.asList(properties);
      return this;
    }

    public <V extends Vectorizer> Configuration vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    public <V extends Vectorizer> Configuration vector(VectorIndex<V> vector) {
      this.vectors = Vectors.of(vector);
      return this;
    }

    public <V extends Vectorizer> Configuration vector(String name, VectorIndex<V> vector) {
      this.vectors = new Vectors(name, vector);
      return this;
    }

    public Configuration vectors(Consumer<NamedVectors> named) {
      this.vectors = Vectors.with(named);
      return this;
    }

    Configuration(Consumer<Configuration> options) {
      options.accept(this);
    }
  }
}
