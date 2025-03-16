package io.weaviate.client6.v1.collections;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.internal.DtoTypeAdapterFactory;

class CollectionDefinitionDTO implements DtoTypeAdapterFactory.DTO<CollectionDefinition> {
  @SerializedName("class")
  String collection;

  @SerializedName("properties")
  List<Property> properties;

  @SerializedName("vectorConfig")
  Vectors vectors;

  @SerializedName("vectorIndexType")
  private VectorIndex.IndexType vectorIndexType;

  @SerializedName("vectorIndexConfig")
  private VectorIndex.IndexingStrategy vectorIndexConfig;

  @SerializedName("vectorizer")
  private Vectorizer vectorizer;

  public CollectionDefinitionDTO(CollectionDefinition colDef) {
    this.collection = colDef.name();
    this.properties = colDef.properties();
    this.vectors = colDef.vectors();

    var unnamed = this.vectors.getUnnamed();
    if (unnamed.isPresent()) {
      var index = unnamed.get();
      this.vectorIndexType = index.type();
      this.vectorIndexConfig = index.configuration();
      this.vectorizer = index.vectorizer();
    }
  }

  public CollectionDefinition toModel() {
    return new CollectionDefinition(collection, properties, vectors);
  }
}
