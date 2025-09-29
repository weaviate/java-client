package io.weaviate.client6.v1.api.backup;

import com.google.gson.annotations.SerializedName;

public enum CompressionLevel {
  @SerializedName("DefaultCompression")
  DEFAULT,
  @SerializedName("BestSpeed")
  BEST_SPEED,
  @SerializedName("BestCompression")
  BEST_COMPRESSION;
}
