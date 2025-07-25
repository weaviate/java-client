package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import lombok.ToString;

/**
 * Vectors is an abstraction over named vectors, which can store
 * both 1-dimensional and 2-dimensional vectors.
 */
@ToString
public class Vectors {
  /** Elements of this map must only be {@code float[]} or {@code float[][]}. */
  private final Map<String, Object> namedVectors;

  public static Vectors of(float[] vector) {
    return of(VectorIndex.DEFAULT_VECTOR_NAME, vector);
  }

  public static Vectors of(String name, float[] vector) {
    return new Vectors(name, vector);
  }

  public static Vectors of(float[][] vector) {
    return of(VectorIndex.DEFAULT_VECTOR_NAME, vector);
  }

  public static Vectors of(String name, float[][] vector) {
    return new Vectors(name, vector);
  }

  public static Vectors of(Function<Builder, ObjectBuilder<Vectors>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Vectors(Builder builder) {
    this.namedVectors = builder.namedVectors;
  }

  /**
   * Create a single named vector.
   *
   * <p>
   * Callers must ensure that vectors are either
   * {@code float[]} or {@code float[][]}.
   *
   * @param name   Vector name.
   * @param vector {@code float[]} or {@code float[][]} vector.
   */
  private Vectors(String name, Object vector) {
    this.namedVectors = Collections.singletonMap(name, vector);
  }

  /**
   * Create a Vectors from a map.
   *
   * <p>
   * Callers must ensure that vectors are either
   * {@code float[]} or {@code float[][]}.
   *
   * @param name   Vector name.
   * @param vector Map of named vectors.
   */
  private Vectors(Map<String, Object> namedVectors) {
    this.namedVectors = namedVectors;
  }

  public static class Builder implements ObjectBuilder<Vectors> {
    private final Map<String, Object> namedVectors = new HashMap<>();

    public Builder vector(String name, float[] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    public Builder vector(String name, float[][] vector) {
      this.namedVectors.put(name, vector);
      return this;
    }

    @Override
    public Vectors build() {
      return new Vectors(this);
    }
  }

  /**
   * Get 1-dimensional vector by name.
   *
   * @returns Vector as {@code float[]} or {@code null}.
   * @throws ClassCastException The underlying vector is not a {@code float[]}.
   */
  public float[] getSingle(String name) {
    return (float[]) namedVectors.get(name);
  }

  /**
   * Get default 1-dimensional vector.
   *
   * @returns Vector as {@code float[]} or {@code null}.
   * @throws ClassCastException if the underlying object is not a {@code float[]}.
   */
  public float[] getDefaultSingle() {
    return getSingle(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Get 2-dimensional vector by name.
   *
   * @returns Vector as {@code float[][]} or {@code null}.
   * @throws ClassCastException if the underlying object is not a
   *                            {@code float[][]}.
   */
  public float[][] getMulti(String name) {
    return (float[][]) namedVectors.get(name);
  }

  /**
   * Get default 2-dimensional vector.
   *
   * @returns Vector as {@code float[][]} or {@code null}.
   * @throws ClassCastException if the underlying object is not a
   *                            {@code float[][]}.
   */
  public float[][] getDefaultMulti() {
    return getMulti(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Get all vectors.
   * Each element is either a {@code float[]} or a {@code float[][]}.
   *
   *
   * @returns Map of name-vector pairs. The returned map is immutable.
   */
  public Map<String, Object> asMap() {
    return Map.copyOf(namedVectors);
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() != Vectors.class) {
        return null;
      }
      final var mapAdapter = gson.getDelegateAdapter(this, new TypeToken<Map<String, Object>>() {
      });
      final var float_1d = gson.getDelegateAdapter(this, TypeToken.get(float[].class));
      final var float_2d = gson.getDelegateAdapter(this, TypeToken.get(float[][].class));
      return (TypeAdapter<T>) new TypeAdapter<Vectors>() {

        @Override
        public void write(JsonWriter out, Vectors value) throws IOException {
          mapAdapter.write(out, value.namedVectors);
        }

        @Override
        public Vectors read(JsonReader in) throws IOException {
          var vectorsMap = JsonParser.parseReader(in).getAsJsonObject().asMap();
          var namedVectors = new HashMap<String, Object>();

          for (var entry : vectorsMap.entrySet()) {
            String vectorName = entry.getKey();
            JsonElement el = entry.getValue();
            if (el.isJsonArray()) {
              JsonArray array = el.getAsJsonArray();
              Object vector;
              if (array.size() > 0 && array.get(0).isJsonArray()) {
                vector = float_2d.fromJsonTree(array);
              } else {
                vector = float_1d.fromJsonTree(array);
              }

              assert (vector instanceof float[]) || (vector instanceof float[][]) : "invalid vector type";
              namedVectors.put(vectorName, vector);
            }
          }
          return new Vectors(namedVectors);
        }
      }.nullSafe();
    }
  }
}
