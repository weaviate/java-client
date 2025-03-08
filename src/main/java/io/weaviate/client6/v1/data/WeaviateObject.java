package io.weaviate.client6.v1.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

// TODO: unify this with collections.SearchObject

@AllArgsConstructor
public class WeaviateObject<T> {
  public final String collection;
  public final T properties;
  public final Metadata metadata;

  @AllArgsConstructor
  public static class Metadata {
    public final String id;
    public final Vectors vectors;
  }

  WeaviateObject(String collection, T properties, Consumer<CustomMetadata> options) {
    var metadata = new CustomMetadata(options);

    this.collection = collection;
    this.properties = properties;
    this.metadata = new Metadata(metadata.id, metadata.vectors);
  }

  RequestObject<T> toRequestObject() {
    return new RequestObject<T>(collection, metadata.id, properties, metadata.vectors.asMap());
  }

  @AllArgsConstructor
  static class RequestObject<T> {
    @SerializedName("class")
    public String collection;
    @SerializedName("id")
    public String id;
    @SerializedName("properties")
    public T properties;
    @SerializedName("vectors")
    public Map<String, Object> vectors;

    WeaviateObject<T> toWeaviateObject() {
      Map<String, Object> arrayVectors = new HashMap<>();
      for (var entry : vectors.entrySet()) {
        var value = (ArrayList<Double>) entry.getValue();
        var vector = new Float[value.size()];
        int i = 0;
        for (var v : value) {
          vector[i++] = v.floatValue();
        }
        arrayVectors.put(entry.getKey(), vector);
      }
      return new WeaviateObject<T>(collection, properties, new Metadata(id, new Vectors(arrayVectors)));
    }
  }
}
