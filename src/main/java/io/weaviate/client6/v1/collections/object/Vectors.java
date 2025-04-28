package io.weaviate.client6.v1.collections.object;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.ToString;

/**
 * Vectors is an abstraction over named vectors.
 * It may contain both 1-dimensional and 2-dimensional vectors.
 */
@ToString
public class Vectors {
  private static final String DEFAULT = "default";

  private Float[] unnamedVector;
  private Map<String, Object> namedVectors;

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

  /** Creates Vectors with a single unnamed vector. */
  private Vectors(Float[] vector) {
    this(Map.of());
    this.unnamedVector = vector;
  }

  /** Creates Vectors with one named vector. */
  private Vectors(String name, Object vector) {
    this.namedVectors = Map.of(name, vector);
  }

  /** Creates immutable set of vectors. */
  private Vectors(Map<String, ? extends Object> vectors) {
    this.namedVectors = Collections.unmodifiableMap(vectors);
  }

  private Vectors(NamedVectors named) {
    this.namedVectors = named.namedVectors;
  }

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
    return new Vectors(vectors);
  }

  public static Vectors of(Consumer<NamedVectors> fn) {
    var named = new NamedVectors();
    fn.accept(named);
    return named.build();
  }

  public static class NamedVectors {
    private Map<String, Object> namedVectors = new HashMap<>();

    public NamedVectors vector(String name, Float[] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    public NamedVectors vector(String name, Float[][] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    public Vectors build() {
      return new Vectors(this);
    }
  }
}
