package io.weaviate.client6.v1.api.backup;

import com.google.gson.annotations.SerializedName;

public enum BackupStatus {
  /** Backup creation / restoration has begun. */
  @SerializedName("STARTED")
  STARTED,
  /** Backup in progress, data is being transferred. */
  @SerializedName("TRANSFERRING")
  TRANSFERRING,
  /**
   * Cancellation has been claimed by a coordinator.
   * Used as a distributed lock to prevent race conditions when multiple
   * coordinators attempt to cancel the same restore.
   */
  @SerializedName("CANCELLING")
  CANCELLING,
  /**
   * File staging is complete and schema changes are being applied.
   * Cancellation is blocked.
   */
  @SerializedName("FINALIZING")
  FINALIZING,
  /** Backup creation / restoration completed successfully. */
  @SerializedName("SUCCESS")
  SUCCESS,
  /** Backup creation / restoration failed. */
  @SerializedName("FAILED")
  FAILED,
  /** Backup creation canceled. */
  @SerializedName("CANCELED")
  CANCELED;
}
