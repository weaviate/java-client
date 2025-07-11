package io.weaviate.client.v1.cluster.api;

import com.google.gson.annotations.SerializedName;

public enum ReplicationType {
  @SerializedName("COPY")
  COPY,
  @SerializedName("MOVE")
  MOVE;
}
