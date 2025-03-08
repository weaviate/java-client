package io.weaviate.client6.v1.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Vectors is an abstraction over named vectors.
 * It may contain both 1-dimensional and 2-dimensional vectors.
 */
public class Vectors {
  private static final String DEFAULT = "default";

  private Map<String, Object> namedVectors;

  public static Vectors of(Float[] vector) {
    return new Vectors(vector);
  }

  public static Vectors of(Float[][] vector) {
    return new Vectors(vector);
  }

  public static Vectors of(String name, Float[] vector) {
    return new Vectors(name, vector);
  }

  public static Vectors of(String name, Float[][] vector) {
    return new Vectors(name, vector);
  }

  /** Creates immutable set of vectors. */
  Vectors(Map<String, ? extends Object> vectors) {
    this.namedVectors = Collections.unmodifiableMap(vectors);
  }

  /** Creates immutable set of vectors. */
  private Vectors(Float[] vector) {
    this.namedVectors = Map.of(DEFAULT, vector);
  }

  /** Creates immutable set of vectors. */
  private Vectors(Float[][] vector) {
    this.namedVectors = Map.of(DEFAULT, vector);
  }

  /** Creates extendable set of vectors. */
  private Vectors(String name, Object vector) {
    this();
    this.namedVectors.put(name, vector);
  }

  Vectors() {
    this.namedVectors = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  public Optional<Float[]> getSingle() {
    return (Optional<Float[]>) getOnly();
  }

  public Optional<Float[]> getSingle(String name) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public Optional<Float[][]> getMulti() {
    return (Optional<Float[][]>) getOnly();
  }

  public Optional<Float[][]> getMulti(String name) {
    return null;
  }

  private Optional<?> getOnly() {
    if (namedVectors == null || namedVectors.isEmpty() || namedVectors.size() > 1) {
      return Optional.empty();
    }
    return Optional.ofNullable(namedVectors.values().iterator().next());
  }

  Map<String, Object> asMap() {
    return Map.copyOf(namedVectors);
  }
}
