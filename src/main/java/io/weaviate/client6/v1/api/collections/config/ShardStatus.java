package io.weaviate.client6.v1.api.collections.config;

import com.google.gson.annotations.SerializedName;

public enum ShardStatus {
  @SerializedName("READY")
  READY,
  @SerializedName("READONLY")
  READONLY;
}
