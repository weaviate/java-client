package io.weaviate.client6.v1.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Vectors {
  public static final String DEFAULT = "default";

  private final VectorIndex<? extends Vectorizer> unnamedVector;
  private final Map<String, VectorIndex<? extends Vectorizer>> namedVectors;

  public static <V extends Vectorizer> Vectors unnamed(VectorIndex<V> vector) {
    return new Vectors(vector);
  }

  public static <V extends Vectorizer> Vectors of(String name, VectorIndex<V> vector) {
    return new Vectors(name, vector);
  }

  public static <V extends Vectorizer> Vectors of(VectorIndex<V> vector) {
    return new Vectors(DEFAULT, vector);
  }

  public static Vectors with(Consumer<NamedVectors> named) {
    var vectors = new NamedVectors(named);
    return new Vectors(vectors.namedVectors);
  }

  public VectorIndex<? extends Vectorizer> get(String name) {
    return namedVectors.get(name);
  }

  public Optional<VectorIndex<? extends Vectorizer>> getUnnamed() {
    return Optional.ofNullable(unnamedVector);
  }

  public VectorIndex<? extends Vectorizer> getDefault() {
    return namedVectors.get(DEFAULT);
  }

  public Map<String, Object> asMap() {
    return Map.copyOf(namedVectors);
  }

  <V extends Vectorizer> Vectors(VectorIndex<V> vector) {
    this.unnamedVector = vector;
    this.namedVectors = Map.of();
  }

  <V extends Vectorizer> Vectors(String name, VectorIndex<V> vector) {
    this.unnamedVector = null;
    this.namedVectors = Map.of(name, vector);
  }

  Vectors(Map<String, VectorIndex<? extends Vectorizer>> vectors) {
    this.unnamedVector = null;
    this.namedVectors = vectors;
  }

  public static class NamedVectors {
    private final Map<String, VectorIndex<? extends Vectorizer>> namedVectors = new HashMap<>();

    public <V extends Vectorizer> NamedVectors vector(String name, VectorIndex<V> vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    NamedVectors(Consumer<NamedVectors> options) {
      options.accept(this);
    }
  }
}
