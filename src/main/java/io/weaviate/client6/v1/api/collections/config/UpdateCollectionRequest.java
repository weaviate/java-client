package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.InvertedIndex;
import io.weaviate.client6.v1.api.collections.MultiTenancy;
import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.Replication;
import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record UpdateCollectionRequest(CollectionConfig updated, CollectionConfig original) {

  public static final Endpoint<UpdateCollectionRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "PUT",
      request -> "/schema/" + request.updated.collectionName(),
      request -> Collections.emptyMap(),
      request -> {
        var json = JSON.serialize(request.updated);

        // Workaround: when doing updates, the server *insists* that
        // "skipDefaultQuantization" property remains unchanged for each vector,
        // even in cases when it is irrelevant [shrug].
        // To mitigate that we will set that field to its original value for all
        // vectors which were present in the original configuration.
        var jsonObject = JSON.toJsonElement(json).getAsJsonObject();
        var vectorsAny = jsonObject.get("vectorConfig");
        if (request.original.vectors() != null && !request.original.vectors().isEmpty()
            && vectorsAny != null && vectorsAny.isJsonObject()) {
          var vectors = vectorsAny.getAsJsonObject();
          for (var origVector : request.original.vectors().entrySet()) {
            var vectorName = origVector.getKey();
            var origQuantization = origVector.getValue().quantization();
            if (vectors.has(vectorName) && origQuantization != null) {
              vectors
                  .get(vectorName).getAsJsonObject()
                  .get("vectorIndexConfig").getAsJsonObject()
                  .addProperty(Quantization.Kind.UNCOMPRESSED.jsonValue(), origQuantization.isUncompressed());
            }
          }

          json = jsonObject.toString();
        }

        return json;
      });

  public static UpdateCollectionRequest of(CollectionConfig original,
      Function<Builder, ObjectBuilder<UpdateCollectionRequest>> fn) {
    return fn.apply(new Builder(original)).build();
  }

  public UpdateCollectionRequest(Builder builder) {
    this(builder.newCollection.build(), builder.currentCollection);
  }

  public static class Builder implements ObjectBuilder<UpdateCollectionRequest> {
    // For updating property descriptions
    private final CollectionConfig currentCollection;
    // Builder for the updated collection config.
    private final CollectionConfig.Builder newCollection;

    public Builder(CollectionConfig currentCollection) {
      this.currentCollection = currentCollection;
      this.newCollection = currentCollection.edit();
    }

    public Builder description(String description) {
      this.newCollection.description(description);
      return this;
    }

    public Builder propertyDescription(String propertyName, String description) {
      for (var property : currentCollection.properties()) {
        if (property.propertyName().equals(propertyName)) {
          var newProperty = property.edit(p -> p.description(description));
          this.newCollection.properties(newProperty);
          break;
        }
      }
      return this;
    }

    public Builder replication(Replication replication) {
      this.newCollection.replication(replication);
      return this;
    }

    public Builder replication(Function<Replication.Builder, ObjectBuilder<Replication>> fn) {
      this.newCollection.replication(fn);
      return this;
    }

    public Builder invertedIndex(InvertedIndex invertedIndex) {
      this.newCollection.invertedIndex(invertedIndex);
      return this;
    }

    public Builder invertedIndex(Function<InvertedIndex.Builder, ObjectBuilder<InvertedIndex>> fn) {
      this.newCollection.invertedIndex(fn);
      return this;
    }

    public Builder rerankerModules(Reranker... rerankerModules) {
      this.newCollection.rerankerModules(rerankerModules);
      return this;
    }

    public Builder rerankerModules(List<Reranker> rerankerModules) {
      this.newCollection.rerankerModules(rerankerModules);
      return this;
    }

    public Builder generativeModule(Generative generativeModule) {
      this.newCollection.generativeModule(generativeModule);
      return this;
    }

    public final Builder vectorConfig(Map<String, VectorConfig> vectors) {
      this.newCollection.vectorConfig(vectors);
      return this;
    }

    @SafeVarargs
    public final Builder vectorConfig(Map.Entry<String, VectorConfig>... vectors) {
      this.newCollection.vectorConfig(vectors);
      return this;
    }

    public Builder multiTenancy(MultiTenancy multiTenancy) {
      this.newCollection.multiTenancy(multiTenancy);
      return this;
    }

    public Builder multiTenancy(Function<MultiTenancy.Builder, ObjectBuilder<MultiTenancy>> fn) {
      this.newCollection.multiTenancy(fn);
      return this;
    }

    @Override
    public UpdateCollectionRequest build() {
      return new UpdateCollectionRequest(this);
    }
  }
}
