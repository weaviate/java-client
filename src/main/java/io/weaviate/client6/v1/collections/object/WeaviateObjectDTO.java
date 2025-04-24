package io.weaviate.client6.v1.collections.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

class WeaviateObjectDTO<T> {
  @SerializedName("class")
  String collection;
  @SerializedName("id")
  String id;
  @SerializedName("properties")
  T properties;
  @SerializedName("vectors")
  Map<String, Object> vectors;

  WeaviateObjectDTO(WeaviateObject<T> object) {
    this.collection = object.collection();
    this.properties = object.properties();

    if (object.metadata() != null) {
      this.id = object.metadata().id();
      if (object.metadata().vectors() != null) {
        this.vectors = object.metadata().vectors().getNamed();
      }
    }
  }

  WeaviateObject<T> toWeaviateObject() {
    Map<String, Object> arrayVectors = new HashMap<>();
    if (vectors != null) {
      for (var entry : vectors.entrySet()) {
        var value = (ArrayList<Double>) entry.getValue();
        var vector = new Float[value.size()];
        int i = 0;
        for (var v : value) {
          vector[i++] = v.floatValue();
        }
        arrayVectors.put(entry.getKey(), vector);
      }
    }

    return new WeaviateObject<T>(collection, properties,
        /* no references through HTTP */ new HashMap<>(),
        new ObjectMetadata(id, Vectors.of(arrayVectors)));
  }
}
