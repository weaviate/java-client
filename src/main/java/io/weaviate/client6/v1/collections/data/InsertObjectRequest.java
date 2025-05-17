package io.weaviate.client6.v1.collections.data;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.collections.Reference;

public record InsertObjectRequest<T>(String collection, T properties, String id, Vectors vectors,
    Map<String, List<Reference>> references) {

  /** Create InsertObjectRequest from Builder options. */
  public InsertObjectRequest(Builder<T> builder) {
    this(builder.collection, builder.properties, builder.id, builder.vectors, builder.references);
  }

  /**
   * Construct InsertObjectRequest with optional parameters.
   *
   * @param <T>        Shape of the object properties, e.g.
   *                   {@code Map<String, Object>}
   * @param collection Collection to insert to.
   * @param properties Object properties.
   * @param fn         Optional parameters
   * @return InsertObjectRequest
   */
  static <T> InsertObjectRequest<T> of(String collection, T properties, Consumer<Builder<T>> fn) {
    var builder = new Builder<>(collection, properties);
    fn.accept(builder);
    return builder.build();
  }

  public static class Builder<T> {
    private final String collection; // Required
    private final T properties; // Required

    private String id;
    private Vectors vectors;
    private final Map<String, List<Reference>> references = new HashMap<>();

    Builder(String collection, T properties) {
      this.collection = collection;
      this.properties = properties;
    }

    /** Define custom object id. Must be a valid UUID. */
    public Builder<T> id(String id) {
      this.id = id;
      return this;
    }

    /**
     * Supply one or more (named) vectors. Calls to {@link #vectors} are not
     * chainable. Use {@link Vectors#of(Consumer)} to pass multiple vectors.
     */
    public Builder<T> vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    /**
     * Add a reference. Calls to {@link #reference} can be chained
     * to add multiple references.
     */
    public Builder<T> reference(String property, Reference... references) {
      for (var ref : references) {
        addReference(property, ref);
      }
      return this;
    }

    private void addReference(String property, Reference reference) {
      if (!references.containsKey(property)) {
        references.put(property, new ArrayList<>());
      }
      references.get(property).add(reference);
    }

    /** Build a new InsertObjectRequest. */
    public InsertObjectRequest<T> build() {
      return new InsertObjectRequest<>(this);
    }
  }

  // Here we're just rawdogging JSON serialization just to get a good feel for it.
  public String serialize(Gson gson) throws IOException {
    var buf = new StringWriter();
    var w = gson.newJsonWriter(buf);

    w.beginObject();

    w.name("class");
    w.value(collection);

    if (id != null) {
      w.name("id");
      w.value(id);
    }

    if (vectors != null) {
      var unnamed = vectors.getUnnamed();
      if (unnamed.isPresent()) {
        w.name("vector");
        gson.getAdapter(Float[].class).write(w, unnamed.get());
      } else {
        w.name("vectors");
        gson.getAdapter(new TypeToken<Map<String, Object>>() {
        }).write(w, vectors.getNamed());
      }
    }

    if (properties != null || references != null) {
      w.name("properties");
      w.beginObject();

      if (properties != null) {
        assert properties instanceof Map : "properties not a Map";
        for (var entry : ((Map<String, Object>) properties).entrySet()) {
          w.name(entry.getKey());
          gson.getAdapter(Object.class).write(w, entry.getValue());
        }

      }
      if (references != null && !references.isEmpty()) {
        for (var entry : references.entrySet()) {
          w.name(entry.getKey());
          w.beginArray();
          for (var ref : entry.getValue()) {
            ref.writeValue(w);
          }
          w.endArray();
        }
      }

      w.endObject();
    }

    w.endObject();
    return buf.toString();
  }
}
