package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

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

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record WriteWeaviateObject<P>(
    String collection,
    P properties,
    Map<String, List<Reference>> references,
    ObjectMetadata metadata,
    String tenant) {

  WriteWeaviateObject(WeaviateObject<P, Reference, ObjectMetadata> object, String tenant) {
    this(object.collection(), object.properties(), object.references(), object.metadata(), tenant);
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      var type = typeToken.getType();
      var rawType = typeToken.getRawType();
      if (rawType != WriteWeaviateObject.class ||
          !(type instanceof ParameterizedType parameterized)
          || parameterized.getActualTypeArguments().length < 1) {
        return null;
      }

      var typeParams = parameterized.getActualTypeArguments();
      final var propertiesType = typeParams[0];

      final var propertiesAdapter = gson.getAdapter(TypeToken.get(propertiesType));
      final var metadataAdapter = gson.getAdapter(ObjectMetadata.class);
      final var referencesAdapter = gson.getAdapter(Reference.class);

      return (TypeAdapter<T>) new TypeAdapter<WriteWeaviateObject<?>>() {

        @Override
        public void write(JsonWriter out, WriteWeaviateObject<?> value) throws IOException {
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
              for (var reference : refEntry.getValue()) {
                var beacon = referencesAdapter.toJsonTree(reference);
                beacons.add(beacon);
              }
              properties.add(refEntry.getKey(), beacons);
            }
            Streams.write(properties, out);
          }

          // Flatten out metadata fields.
          var metadata = metadataAdapter.toJsonTree(value.metadata);
          for (var entry : metadata.getAsJsonObject().entrySet()) {
            out.name(entry.getKey());
            Streams.write(entry.getValue(), out);
          }

          out.name("tenant");
          out.value(value.tenant());

          out.endObject();
        }

        @Override
        public WriteWeaviateObject<?> read(JsonReader in) throws IOException {
          var builder = new WeaviateObject.Builder<Object, Reference, ObjectMetadata>();
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
                var beacon = referencesAdapter.fromJsonTree(el);
                builder.reference(property.getKey(), (Reference) beacon);
              }
            }
          }

          builder.properties(propertiesAdapter.fromJsonTree(trueProperties));

          metadata.uuid(object.get("id").getAsString());
          builder.metadata(metadata.build());

          var tenant = object.get("tenant");
          return new WriteWeaviateObject<>(builder.build(), tenant != null ? tenant.getAsString() : "");
        }
      }.nullSafe();
    }
  }
}
