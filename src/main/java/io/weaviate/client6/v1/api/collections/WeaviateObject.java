package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record WeaviateObject<P, M extends WeaviateMetadata>(
    String collection,
    P properties,
    Map<String, ObjectReference<P, M>> references,
    M metadata) {

  public WeaviateObject(Builder<P, M> builder) {
    this(builder.collection, builder.properties, builder.references, builder.metadata);
  }

  public static class Builder<P, M extends WeaviateMetadata> implements ObjectBuilder<WeaviateObject<P, M>> {
    private String collection;
    private P properties;
    private Map<String, ObjectReference<P, M>> references;
    private M metadata;

    public Builder<P, M> collection(String collection) {
      this.collection = collection;
      return this;
    }

    public Builder<P, M> properties(P properties) {
      this.properties = properties;
      return this;
    }

    public Builder<P, M> references(Map<String, ObjectReference<P, M>> references) {
      this.references = references;
      return this;
    }

    public Builder<P, M> metadata(M metadata) {
      this.metadata = metadata;
      return this;
    }

    @Override
    public WeaviateObject<P, M> build() {
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
          !(type instanceof ParameterizedType parameterized)) {
        return null;
      }

      var typeParams = parameterized.getActualTypeArguments();
      final var propertiesType = typeParams[0];
      final var metadataType = typeParams[1];

      final var propertiesAdapter = gson.getAdapter(TypeToken.get(propertiesType));
      final var metadataAdapter = gson.getAdapter(TypeToken.get(metadataType));

      final var referencesAdapter = gson.getAdapter(TypeToken.getParameterized(
          Map.class,
          String.class, TypeToken.getParameterized(
              ObjectReference.class, propertiesType, metadataType)
              .getType()));

      return (TypeAdapter<T>) new TypeAdapter<WeaviateObject<?, ?>>() {

        @Override
        public void write(JsonWriter out, WeaviateObject<?, ?> value) throws IOException {
          out.beginObject();

          out.name("class");
          out.value(value.collection());

          out.name("properties");
          ((TypeAdapter<Object>) propertiesAdapter).write(out, value.properties());

          out.name("references");
          ((TypeAdapter<Object>) referencesAdapter).write(out, value.references());

          // Flatten out metadata fields.
          var metadata = ((TypeAdapter<Object>) metadataAdapter).toJsonTree(value.metadata);
          for (var entry : metadata.getAsJsonObject().entrySet()) {
            out.name(entry.getKey());
            Streams.write(entry.getValue(), out);
          }
          out.endObject();
        }

        @Override
        public WeaviateObject<?, ?> read(JsonReader in) throws IOException {
          var builder = new WeaviateObject.Builder<>();
          var metadata = new ObjectMetadata.Builder();

          in.beginObject();
          while (in.hasNext()) {
            switch (in.nextName()) {
              case "class":
                builder.collection(in.nextString());
                break;
              case "properties":
                var properties = propertiesAdapter.read(in);
                builder.properties(properties);
                break;
              case "references":
                var references = referencesAdapter.read(in);
                builder.references((Map<String, ObjectReference<Object, WeaviateMetadata>>) references);
                break;

              // Collect metadata
              case "id":
                metadata.id(in.nextString());
                break;
              default: // ignore unknown values
                in.skipValue();
                break;
            }
          }
          in.endObject();

          builder.metadata(metadata.build());
          return builder.build();
        }
      };
    }
  }
}
