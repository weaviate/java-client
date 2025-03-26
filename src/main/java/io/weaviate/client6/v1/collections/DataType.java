package io.weaviate.client6.v1.collections;

import com.google.gson.annotations.SerializedName;

public enum DataType {
  @SerializedName("text")
  TEXT,
  @SerializedName("int")
  INT,
  @SerializedName("reference")
  REFERENCE;
}
