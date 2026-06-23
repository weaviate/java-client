package io.weaviate.client6.v1.api.export;

import com.google.gson.annotations.SerializedName;

/** Export status for an individual collection shard. */
public enum ShardExportStatus {
  /** Export in progress, data is being transferred. */
  @SerializedName("TRANSFERRING")
  TRANSFERRING,
  /** Export creation completed successfully. */
  @SerializedName("SUCCESS")
  SUCCESS,
  /** Export creation failed. */
  @SerializedName("FAILED")
  FAILED,
  /** Shard data will not be included in the snapshot. */
  @SerializedName("SKIPPED")
  SKIPPED;
}
