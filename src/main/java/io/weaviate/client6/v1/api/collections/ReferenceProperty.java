package io.weaviate.client6.v1.api.collections;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ReferenceProperty(
    @SerializedName("name") String propertyName,
    @SerializedName("dataType") List<String> dataTypes) {

  public Property toProperty() {
    return new Property.Builder(propertyName, dataTypes).build();
  }
}
