package io.weaviate.client6.v1.collections.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.collections.CollectionDefinition;
import io.weaviate.client6.v1.collections.Property;

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
}
