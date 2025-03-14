package io.weaviate.client6.v1.collections;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class CollectionDefinitionDTO {
  @SerializedName("class")
  String collection;

  @SerializedName("properties")
  List<Property> properties;

  @SerializedName("vectorConfig")
  Vectors vectors;

  public CollectionDefinitionDTO(CollectionDefinition colDef) {
    this.collection = colDef.name();
    this.properties = colDef.properties();
    this.vectors = colDef.vectors();
  }

  CollectionDefinition toCollectionDefinition() {
    return new CollectionDefinition(collection, properties, vectors);
  }
}
