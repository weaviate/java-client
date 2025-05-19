package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import lombok.ToString;

/**
 * Vectors is an abstraction over named vectors.
 * It may contain both 1-dimensional and 2-dimensional vectors.
 */
@ToString
public class Vectors {
  // TODO: define this in collection.config.Vectors
  private static final String DEFAULT = "default";

  private final Float[] unnamedVector;
  private final Map<String, Object> namedVectors;

  /**
   * Pass legacy unnamed vector.
   * Multi-vectors can only be passed as named vectors.
   */
  public static Vectors unnamed(Float[] vector) {
    return new Vectors(vector);
  }

  public static Vectors of(Float[] vector) {
    return new Vectors(DEFAULT, vector);
  }

  public static Vectors of(Float[][] vector) {
    return new Vectors(DEFAULT, vector);
  }

  public static Vectors of(String name, Float[] vector) {
    return new Vectors(name, vector);
  }

  public static Vectors of(String name, Float[][] vector) {
    return new Vectors(name, vector);
  }

  public static Vectors of(Map<String, ? extends Object> vectors) {
    return new Vectors(vectors, null);
  }

  public static Vectors of(Function<Builder, ObjectBuilder<Vectors>> fn) {
    return fn.apply(new Builder()).build();
  }

  public static class Builder {
    private Map<String, Object> namedVectors = new HashMap<>();

    public Builder vector(String name, Float[] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    public Builder vector(String name, Float[][] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    public Vectors build() {
      return new Vectors(this.namedVectors, null);
    }
  }

  public Float[] getSingle(String name) {
    return (Float[]) namedVectors.get(name);
  }

  public Float[] getDefaultSingle() {
    return getSingle(DEFAULT);
  }

  @SuppressWarnings("unchecked")
  public Optional<Float[]> getSingle() {
    return (Optional<Float[]>) getOnly();
  }

  public Float[][] getMulti(String name) {
    return (Float[][]) namedVectors.get(name);
  }

  public Float[][] getDefaultMulti() {
    return getMulti(DEFAULT);
  }

  @SuppressWarnings("unchecked")
  public Optional<Float[][]> getMulti() {
    return (Optional<Float[][]>) getOnly();
  }

  public Optional<Float[]> getUnnamed() {
    return Optional.ofNullable(unnamedVector);
  }

  private Optional<?> getOnly() {
    if (namedVectors == null || namedVectors.isEmpty() || namedVectors.size() > 1) {
      return Optional.empty();
    }
    return Optional.ofNullable(namedVectors.values().iterator().next());
  }

  public Map<String, Object> getNamed() {
    return Map.copyOf(namedVectors);
  }

  private Vectors(Map<String, ? extends Object> named) {
    this(named, null);
  }

  private Vectors(Float[] unnamed) {
    this(Collections.emptyMap(), unnamed);
  }

  private Vectors(String name, Object vector) {
    this(Collections.singletonMap(name, vector));
  }

  private Vectors(Map<String, ? extends Object> named, Float[] unnamed) {
    this.namedVectors = Map.copyOf(named);
    this.unnamedVector = unnamed;
  }
}
