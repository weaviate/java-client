package io.weaviate.client6.v1.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.internal.DtoTypeAdapterFactory;

class CollectionDefinitionDTO implements DtoTypeAdapterFactory.Dto<Collection> {
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

  public CollectionDefinitionDTO(Collection colDef) {
    this.collection = colDef.name();
    this.properties = Stream.concat(
        colDef.properties().stream(),
        colDef.references().stream().map(r -> new Property(r.name(),
            r.dataTypes())))
        .toList();
    this.vectors = colDef.vectors();

    if (this.vectors != null) {
      var unnamed = this.vectors.getUnnamed();
      if (unnamed.isPresent()) {
        var index = unnamed.get();
        this.vectorIndexType = index.type();
        this.vectorIndexConfig = index.configuration();
        this.vectorizer = index.vectorizer();
      }
    }
  }

  public Collection toModel() {
    var onlyProperties = new ArrayList<Property>();
    var references = new ArrayList<ReferenceProperty>();

    for (var p : properties) {
      if (p.isReference()) {
        references.add(Property.reference(p.name(), p.dataTypes()));
      } else {
        onlyProperties.add(p);
      }
    }
    return new Collection(collection, onlyProperties, references, vectors);
  }
}
