package io.weaviate.client6.v1.api.backup;

import com.google.gson.annotations.SerializedName;

public enum CompressionLevel {
  /** Use default compression algorithm (gzip). */
  @SerializedName("DefaultCompression")
  DEFAULT,
  /** Use compression algorithm that prioritizes speed. */
  @SerializedName("BestSpeed")
  BEST_SPEED,
  /** Use compression algorithm that prioritizes compression quality. */
  @SerializedName("BestCompression")
  BEST_COMPRESSION;
}
