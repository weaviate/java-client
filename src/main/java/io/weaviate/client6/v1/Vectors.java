package io.weaviate.client6.v1;

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

  private Float[] legacyVector;
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
    return Optional.ofNullable(legacyVector);
  }

  private Optional<?> getOnly() {
    if (namedVectors == null || namedVectors.isEmpty() || namedVectors.size() > 1) {
      return Optional.empty();
    }
    return Optional.ofNullable(namedVectors.values().iterator().next());
  }

  public Map<String, Object> asMap() {
    return Map.copyOf(namedVectors);
  }

  /** Creates Vectors with a single unnamed vector. */
  private Vectors(Float[] vector) {
    this.legacyVector = vector;
    this.namedVectors = Map.of();
  }

  /** Creates immutable set of vectors. */
  public Vectors(Map<String, ? extends Object> vectors) {
    this.namedVectors = Collections.unmodifiableMap(vectors);
  }

  /**
   * Pass legacy unnamed vector.
   * Multi-vectors can only be passed as named vectors.
   */
  public static Vectors unnamed(Float[] vector) {
    return new Vectors(vector);
  }

  public static Vectors.Builder of(Float[] vector) {
    return Vectors.of(DEFAULT, vector);
  }

  public static Vectors.Builder of(Float[][] vector) {
    return Vectors.of(DEFAULT, vector);
  }

  public static Vectors.Builder of(String name, Float[] vector) {
    return new Vectors.Builder(name, vector);
  }

  public static Vectors.Builder of(String name, Float[][] vector) {
    return new Vectors.Builder(name, vector);
  }

  public static class Builder {
    private Map<String, Object> namedVectors = new HashMap<>();

    // Hide this constructor;
    private Builder() {
    }

    public Builder(String name, Float[] vector) {
      this.namedVectors.put(name, vector);
    }

    public Builder(String name, Float[][] vector) {
      this.namedVectors.put(name, vector);
    }

    public Builder of(String name, Float[] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    public Builder of(String name, Float[][] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    // If we could bring both Data and Query methods in one package,
    // then we wouldn't need to expose this method.
    // Alternatively, of course, we go with Tucked Builders all the way.
    public Vectors build() {
      return new Vectors(namedVectors);
    }
  }
}
