package io.weaviate.client6.v1.api.backup;

import com.google.gson.annotations.SerializedName;

public enum BackupStatus {
  /** Backup creation / restoration has begun. */
  @SerializedName("STARTED")
  STARTED,
  /** Backup in progress, data is being transferred. */
  @SerializedName("TRANSFERRING")
  TRANSFERRING,
  /** Backup creation / restoration completed successfully. */
  @SerializedName("SUCCESS")
  SUCCESS,
  /** Backup creation / restoration failed. */
  @SerializedName("FAILED")
  FAILED,
  /**
   * Backup creation canceled.
   * This status is never returned for backup restorations.
   */
  @SerializedName("CANCELED")
  CANCELED;
}
