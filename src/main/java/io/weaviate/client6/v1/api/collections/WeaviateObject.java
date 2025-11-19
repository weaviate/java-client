package io.weaviate.client6.v1.api.collections;

public interface WeaviateObject {
  String uuid();

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record WeaviateObject<P, R, M extends WeaviateMetadata>(
    String collection,
    P properties,
    Map<String, List<R>> references,
    M metadata) {

  /** Shorthand for accessing objects's UUID from metadata. */
  public String uuid() {
    return metadata.uuid();
  }

  /** Shorthand for accessing objects's vectors from metadata. */
  public Vectors vectors() {
    return metadata.vectors();
  }

  public static <P, R, M extends WeaviateMetadata> WeaviateObject<P, R, M> of(
      Function<Builder<P, R, M>, ObjectBuilder<WeaviateObject<P, R, M>>> fn) {
    return fn.apply(new Builder<>()).build();
  }

  public WeaviateObject(Builder<P, R, M> builder) {
    this(builder.collectionName, builder.properties, builder.references, builder.metadata);
  }

  public static class Builder<P, R, M extends WeaviateMetadata> implements ObjectBuilder<WeaviateObject<P, R, M>> {
    private String collectionName;
    private P properties;
    private Map<String, List<R>> references = new HashMap<>();
    private M metadata;

    /** Set the name of the collection his object belongs to. */
    public final Builder<P, R, M> collection(String collectionName) {
      this.collectionName = collectionName;
      return this;
    }

    /** Add object properties. */
    public final Builder<P, R, M> properties(P properties) {
      this.properties = properties;
      return this;
    }

    /**
     * Add a reference. Calls to {@link #reference} can be chained
     * to add multiple references.
     */
    @SafeVarargs
    public final Builder<P, R, M> reference(String property, R... references) {
      for (var ref : references) {
        addReference(property, ref);
      }
      return this;
    }

    private final void addReference(String property, R reference) {
      if (!references.containsKey(property)) {
        references.put(property, new ArrayList<>());
      }
      references.get(property).add(reference);
    }

    public Builder<P, R, M> references(Map<String, List<R>> references) {
      this.references = references;
      return this;
    }

    public Builder<P, R, M> metadata(M metadata) {
      this.metadata = metadata;
      return this;
    }

    @Override
    public WeaviateObject<P, R, M> build() {
      return new WeaviateObject<>(this);
    }
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      var type = typeToken.getType();
      var rawType = typeToken.getRawType();
      if (rawType != WeaviateObject.class ||
          !(type instanceof ParameterizedType parameterized)
          || parameterized.getActualTypeArguments().length < 3) {
        return null;
      }

      var typeParams = parameterized.getActualTypeArguments();
      final var propertiesType = typeParams[0];
      final var referencesType = typeParams[1];
      final var metadataType = typeParams[2];

      final var propertiesAdapter = gson.getAdapter(TypeToken.get(propertiesType));
      final var metadataAdapter = gson.getAdapter(TypeToken.get(metadataType));
      final var referencesAdapter = gson.getAdapter(TypeToken.get(referencesType));

      return (TypeAdapter<T>) new TypeAdapter<WeaviateObject<?, ?, ?>>() {

        @Override
        public void write(JsonWriter out, WeaviateObject<?, ?, ?> value) throws IOException {
          out.beginObject();

          out.name("class");
          out.value(value.collection());

          out.name("properties");
          if (value.references().isEmpty()) {
            ((TypeAdapter<Object>) propertiesAdapter).write(out, value.properties());
          } else {
            var properties = ((TypeAdapter<Object>) propertiesAdapter).toJsonTree(value.properties()).getAsJsonObject();
            for (var refEntry : value.references().entrySet()) {
              var beacons = new JsonArray();
              for (var reference : (List<Object>) refEntry.getValue()) {
                var beacon = ((TypeAdapter<Object>) referencesAdapter).toJsonTree(reference);
                beacons.add(beacon);
              }
              properties.add(refEntry.getKey(), beacons);
            }
            Streams.write(properties, out);
          }

          // Flatten out metadata fields.
          var metadata = ((TypeAdapter<Object>) metadataAdapter).toJsonTree(value.metadata);
          for (var entry : metadata.getAsJsonObject().entrySet()) {
            out.name(entry.getKey());
            Streams.write(entry.getValue(), out);
          }
          out.endObject();
        }

        @Override
        public WeaviateObject<?, ?, ?> read(JsonReader in) throws IOException {
          var builder = new WeaviateObject.Builder<>();
          var metadata = new ObjectMetadata.Builder();

          var object = JsonParser.parseReader(in).getAsJsonObject();
          builder.collection(object.get("class").getAsString());

          var jsonProperties = object.get("properties").getAsJsonObject();
          var trueProperties = new JsonObject();
          for (var property : jsonProperties.entrySet()) {
            var value = property.getValue();
            if (!value.isJsonArray()) {
              trueProperties.add(property.getKey(), value);
              continue;
            }
            var array = value.getAsJsonArray();
            var first = array.get(0);
            if (first.isJsonObject() && first.getAsJsonObject().has("beacon")) {
              for (var el : array) {
                var beacon = ((TypeAdapter<Object>) referencesAdapter).fromJsonTree(el);
                builder.reference(property.getKey(), beacon);
              }
            }
          }

          builder.properties(propertiesAdapter.fromJsonTree(trueProperties));

          metadata.uuid(object.get("id").getAsString());
          builder.metadata(metadata.build());

          return builder.build();
        }
      }.nullSafe();
    }
  }
}
