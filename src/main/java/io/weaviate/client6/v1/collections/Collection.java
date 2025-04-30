package io.weaviate.client6.v1.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.weaviate.client6.v1.collections.Vectors.NamedVectors;

public record Collection(String name, List<Property> properties, Vectors vectors) {

  public static Collection with(String name, Consumer<Builder> options) {
    var config = new Builder(options);
    return new Collection(name, config.properties, config.vectors);
  }

  public static class Builder {
    private List<Property> properties = new ArrayList<>();
    private Vectors vectors;

    public Builder properties(Property... properties) {
      this.properties = Arrays.asList(properties);
      return this;
    }

    public <V extends Vectorizer> Builder vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    public <V extends Vectorizer> Builder vector(VectorIndex<V> vector) {
      this.vectors = Vectors.of(vector);
      return this;
    }

    public <V extends Vectorizer> Builder vector(String name, VectorIndex<V> vector) {
      this.vectors = new Vectors(name, vector);
      return this;
    }

    public Builder vectors(Consumer<NamedVectors> named) {
      this.vectors = Vectors.with(named);
      return this;
    }

    Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }
}
