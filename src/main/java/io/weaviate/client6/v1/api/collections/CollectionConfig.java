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
    @SerializedName("vectorConfig") Map<String, VectorConfig> vectors,
    @SerializedName("multiTenancyConfig") MultiTenancy multiTenancy,
    @SerializedName("shardingConfig") Sharding sharding,
    @SerializedName("replicationConfig") Replication replication,
    @SerializedName("invertedIndexConfig") InvertedIndex invertedIndex,
    List<Reranker> rerankerModules,
    Generative generativeModule) {

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
        .vectorConfig(vectors)
        .multiTenancy(multiTenancy)
        .sharding(sharding)
        .replication(replication)
        .invertedIndex(invertedIndex)
        .rerankerModules(rerankerModules != null ? rerankerModules : new ArrayList<>())
        .generativeModule(generativeModule);
  }

  /** Create a copy of this {@code WeaviateCollection} and edit parts of it. */
  public CollectionConfig edit(Function<Builder, ObjectBuilder<CollectionConfig>> fn) {
    return fn.apply(edit()).build();
  }

  public CollectionConfig(Builder builder) {
    this(
        builder.collectionName,
        builder.description,
        builder.propertyList(),
        builder.referenceList(),
        builder.vectors,
        builder.multiTenancy,
        builder.sharding,
        builder.replication,
        builder.invertedIndex,
        builder.rerankerModules,
        builder.generativeModule);
  }

  public static class Builder implements ObjectBuilder<CollectionConfig> {
    // Required parameters;
    private final String collectionName;

    private String description;
    private Map<String, Property> properties = new HashMap<>();
    private Map<String, ReferenceProperty> references = new HashMap<>();
    private Map<String, VectorConfig> vectors = new HashMap<>();
    private MultiTenancy multiTenancy;
    private Sharding sharding;
    private Replication replication;
    private InvertedIndex invertedIndex;
    private List<Reranker> rerankerModules = new ArrayList<>();
    private Generative generativeModule;

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
      properties.forEach(property -> this.properties.put(property.propertyName(), property));
      return this;
    }

    private List<Property> propertyList() {
      return this.properties.values().stream().toList();
    }

    public Builder references(ReferenceProperty... references) {
      return references(Arrays.asList(references));
    }

    public Builder references(List<ReferenceProperty> references) {
      references.forEach(reference -> this.references.put(reference.propertyName(), reference));
      return this;
    }

    private List<ReferenceProperty> referenceList() {
      return this.references.values().stream().toList();
    }

    public final Builder vectorConfig(Map<String, VectorConfig> vectors) {
      this.vectors.putAll(vectors);
      return this;
    }

    @SafeVarargs
    public final Builder vectorConfig(Map.Entry<String, VectorConfig>... vectors) {
      this.vectors.putAll(Map.ofEntries(vectors));
      return this;
    }

    public Builder sharding(Sharding sharding) {
      this.sharding = sharding;
      return this;
    }

    public Builder sharding(Function<Sharding.Builder, ObjectBuilder<Sharding>> fn) {
      this.sharding = Sharding.of(fn);
      return this;
    }

    public Builder multiTenancy(MultiTenancy multiTenancy) {
      this.multiTenancy = multiTenancy;
      return this;
    }

    public Builder multiTenancy(Function<MultiTenancy.Builder, ObjectBuilder<MultiTenancy>> fn) {
      this.multiTenancy = MultiTenancy.of(fn);
      return this;
    }

    public Builder replication(Replication replication) {
      this.replication = replication;
      return this;
    }

    public Builder replication(Function<Replication.Builder, ObjectBuilder<Replication>> fn) {
      this.replication = Replication.of(fn);
      return this;
    }

    public Builder invertedIndex(InvertedIndex invertedIndex) {
      this.invertedIndex = invertedIndex;
      return this;
    }

    public Builder invertedIndex(Function<InvertedIndex.Builder, ObjectBuilder<InvertedIndex>> fn) {
      this.invertedIndex = InvertedIndex.of(fn);
      return this;
    }

    public Builder rerankerModules(Reranker... rerankerModules) {
      return rerankerModules(Arrays.asList(rerankerModules));
    }

    public Builder rerankerModules(List<Reranker> rerankerModules) {
      this.rerankerModules.addAll(rerankerModules);
      return this;
    }

    public Builder generativeModule(Generative generativeModule) {
      this.generativeModule = generativeModule;
      return this;
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

          // References must be merged with properties.
          var references = jsonObject.remove("references").getAsJsonArray();
          var properties = jsonObject.get("properties").getAsJsonArray();
          properties.addAll(references);

          // Reranker and Generative module configs belong to the "moduleConfig".
          var rerankerModules = jsonObject.remove("rerankerModules").getAsJsonArray();
          var generativeModule = jsonObject.remove("generativeModule");
          if (!rerankerModules.isEmpty() || !generativeModule.isJsonNull()) {
            var modules = new JsonObject();

            // Copy configuration for each reranker module.
            rerankerModules.forEach(reranker -> {
              reranker.getAsJsonObject().entrySet()
                  .stream().forEach(entry -> modules.add(entry.getKey(), entry.getValue()));
            });

            // Copy configuration for each generative module.
            generativeModule.getAsJsonObject().entrySet()
                .stream().forEach(entry -> modules.add(entry.getKey(), entry.getValue()));

            jsonObject.add("moduleConfig", modules);
          }

          Streams.write(jsonObject, out);
        }

        @Override
        public CollectionConfig read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          var references = new JsonArray();
          var properties = new JsonArray();

          if (jsonObject.has("properties") && jsonObject.get("properties").isJsonArray()) {
            var mixedProperties = jsonObject.get("properties").getAsJsonArray();

            for (var property : mixedProperties) {
              var dataTypes = property.getAsJsonObject().get("dataType").getAsJsonArray();
              if (dataTypes.size() == 1 && DataType.KNOWN_TYPES.contains(dataTypes.get(0).getAsString())) {
                properties.add(property);
              } else {
                references.add(property);
              }
            }
          }

          jsonObject.add("properties", properties);
          jsonObject.add("references", references);

          if (!jsonObject.has("vectorConfig")) {
            jsonObject.add("vectorConfig", new JsonObject());
          }

          // Separate modules into reranker- and generative- modules.
          var rerankerModules = new JsonArray();
          if (jsonObject.has("moduleConfig") && jsonObject.get("moduleConfig").isJsonObject()) {
            var moduleConfig = jsonObject.remove("moduleConfig").getAsJsonObject();

            moduleConfig.entrySet().stream()
                .forEach(entry -> {
                  var module = new JsonObject();
                  var name = entry.getKey();
                  module.add(name, entry.getValue());

                  if (name.startsWith("reranker-")) {
                    rerankerModules.add(module);
                  } else if (name.startsWith("generative-")) {
                    jsonObject.add("generativeModule", module);
                  }
                });
          }
          jsonObject.add("rerankerModules", rerankerModules);

          return delegate.fromJsonTree(jsonObject);
        }
      }.nullSafe();
    }
  }
}
