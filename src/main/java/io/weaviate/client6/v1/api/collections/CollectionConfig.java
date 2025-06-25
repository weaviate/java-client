package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record CollectionConfig(
    @SerializedName("class") String collectionName,
    @SerializedName("description") String description,
    @SerializedName("properties") List<Property> properties,
    List<ReferenceProperty> references,
    @SerializedName("vectorConfig") Map<String, VectorIndex> vectors) {

  public static CollectionConfig of(String collectionName) {
    return of(collectionName, ObjectBuilder.identity());
  }

  public static CollectionConfig of(String collectionName, Function<Builder, ObjectBuilder<CollectionConfig>> fn) {
    return fn.apply(new Builder(collectionName)).build();
  }

  /**
   * Returns a {@link Builder} with all current values of
   * {@code WeaviateCollection} pre-filled.
   */
  public Builder edit() {
    return new Builder(collectionName)
        .description(description)
        .properties(properties)
        .references(references)
        .vectors(vectors);
  }

  /** Create a copy of this {@code WeaviateCollection} and edit parts of it. */
  public CollectionConfig edit(Function<Builder, ObjectBuilder<CollectionConfig>> fn) {
    return fn.apply(edit()).build();
  }

  public CollectionConfig(Builder builder) {
    this(
        builder.collectionName,
        builder.description,
        builder.properties,
        builder.references,
        builder.vectors);
  }

  public static class Builder implements ObjectBuilder<CollectionConfig> {
    // Required parameters;
    private final String collectionName;

    private String description;
    private List<Property> properties = new ArrayList<>();
    private List<ReferenceProperty> references = new ArrayList<>();
    private Map<String, VectorIndex> vectors = new HashMap<>();

    public Builder(String collectionName) {
      this.collectionName = collectionName;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder properties(Property... properties) {
      return properties(Arrays.asList(properties));
    }

    public Builder properties(List<Property> properties) {
      this.properties.addAll(properties);
      return this;
    }

    public Builder references(ReferenceProperty... references) {
      return references(Arrays.asList(references));
    }

    public Builder references(List<ReferenceProperty> references) {
      this.references.addAll(references);
      return this;
    }

    public Builder vector(VectorIndex vector) {
      this.vectors.put(VectorIndex.DEFAULT_VECTOR_NAME, vector);
      return this;
    }

    public Builder vector(String name, VectorIndex vector) {
      this.vectors.put(name, vector);
      return this;
    }

    public Builder vectors(Map<String, VectorIndex> vectors) {
      this.vectors.putAll(vectors);
      return this;
    }

    public Builder vectors(Function<VectorsBuilder, ObjectBuilder<Map<String, VectorIndex>>> fn) {
      this.vectors = fn.apply(new VectorsBuilder()).build();
      return this;
    }

    public static class VectorsBuilder implements ObjectBuilder<Map<String, VectorIndex>> {
      private Map<String, VectorIndex> vectors = new HashMap<>();

      public VectorsBuilder vector(String name, VectorIndex vector) {
        vectors.put(name, vector);
        return this;
      }

      @Override
      public Map<String, VectorIndex> build() {
        return this.vectors;
      }
    }

    @Override
    public CollectionConfig build() {
      return new CollectionConfig(this);
    }
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() != CollectionConfig.class) {
        return null;
      }

      final var delegate = gson.getDelegateAdapter(this, (TypeToken<CollectionConfig>) type);
      return (TypeAdapter<T>) new TypeAdapter<CollectionConfig>() {

        @Override
        public void write(JsonWriter out, CollectionConfig value) throws IOException {
          var jsonObject = delegate.toJsonTree(value).getAsJsonObject();

          var references = jsonObject.remove("references").getAsJsonArray();
          var properties = jsonObject.get("properties").getAsJsonArray();
          properties.addAll(references);

          Streams.write(jsonObject, out);
        }

        @Override
        public CollectionConfig read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          var mixedProperties = jsonObject.get("properties").getAsJsonArray();
          var references = new JsonArray();
          var properties = new JsonArray();

          for (var property : mixedProperties) {
            var dataTypes = property.getAsJsonObject().get("dataType").getAsJsonArray();
            if (dataTypes.size() == 1 && DataType.KNOWN_TYPES.contains(dataTypes.get(0).getAsString())) {
              properties.add(property);
            } else {
              references.add(property);
            }
          }

          jsonObject.add("properties", properties);
          jsonObject.add("references", references);

          if (!jsonObject.has("vectorConfig")) {
            jsonObject.add("vectorConfig", new JsonObject());
          }

          return delegate.fromJsonTree(jsonObject);
        }
      }.nullSafe();
    }
  }
}
