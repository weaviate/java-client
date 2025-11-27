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
  BEST_COMPRESSION,
  /** Use ZSTD compression algorithm at default (balanced) settings. */
  @SerializedName("ZstdDefaultCompression")
  ZSTD_DEFAULT,
  /** Use ZSTD compression algorithm and prioritize speed. */
  @SerializedName("ZstdBestSpeed")
  ZSTD_BEST_SPEED,
  /** Use ZSTD compression algorithm and prioritize compression quality. */
  @SerializedName("ZstdBestCompression")
  ZSTD_BEST_COMPRESSION,
  /** Do not use compression. */
  @SerializedName("NoCompression")
  NO_COMPRESSION;
}
