package io.weaviate.client6.v1.api.export;

import com.google.gson.annotations.SerializedName;

public enum ExportStatus {
  /** Export creation has begun. */
  @SerializedName("STARTED")
  STARTED,
  /** Export in progress, data is being transferred. */
  @SerializedName("TRANSFERRING")
  TRANSFERRING,
  /** Export creation completed successfully. */
  @SerializedName("SUCCESS")
  SUCCESS,
  /** Export creation failed. */
  @SerializedName("FAILED")
  FAILED,
  /** Export creation canceled. */
  @SerializedName("CANCELED")
  CANCELED;
}
