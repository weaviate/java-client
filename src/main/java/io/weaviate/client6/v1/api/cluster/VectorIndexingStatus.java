package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

public enum VectorIndexingStatus {
  @SerializedName("READONLY")
  READONLY,
  @SerializedName("INDEXING")
  INDEXING,
  @SerializedName("READY")
  READY;
}
