package io.weaviate.client6.v1.collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.collections.CollectionDefinition.VectorConfig;

public class CollectionDefinitionDTO {
  @SerializedName("class")
  String collection;

  @SerializedName("properties")
  List<Property> properties;

  @SerializedName("vectorConfig")
  Map<String, Object> vectorIndices;

  public CollectionDefinitionDTO(CollectionDefinition colDef) {
    this.collection = colDef.name;
    this.properties = colDef.properties;

    this.vectorIndices = new HashMap<>();
    for (var entry : colDef.vectorConfig.entrySet()) {
      var index = entry.getValue();
      this.vectorIndices.put(entry.getKey(), Map.of(
          "vectorizer", index.vectorizer(),
          "vectorIndexType", index.indexType(),
          "vectorIndexConfig", index.indexConfiguration()));
    }
  }

  CollectionDefinition toCollectionDefinition() {
    return new CollectionDefinition(
        collection,
        properties,
        vectorIndices.entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey, entry -> (VectorConfig) entry.getValue())));
  }
}
