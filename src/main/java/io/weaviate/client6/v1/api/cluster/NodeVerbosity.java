package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.json.JsonEnum;

public enum NodeVerbosity implements JsonEnum<NodeVerbosity> {
  @SerializedName("minimal")
  MINIMAL("minimal"),
  @SerializedName("verbose")
  VERBOSE("verbose");

  private final String jsonValue;

  private NodeVerbosity(String jsonValue) {
    this.jsonValue = jsonValue;
  }

  @Override
  public String jsonValue() {
    return jsonValue;
  }

  @Override
  public String toString() {
    return jsonValue();
  }
}
