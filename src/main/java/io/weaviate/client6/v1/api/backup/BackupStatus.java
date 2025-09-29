package io.weaviate.client6.v1.api.backup;

import com.google.gson.annotations.SerializedName;

public enum BackupStatus {
  @SerializedName("STARTED")
  STARTED,
  @SerializedName("TRANSFERRING")
  TRANSFERRING,
  @SerializedName("SUCCESS")
  SUCCESS,
  @SerializedName("FAILED")
  FAILED,
  @SerializedName("CANCELED")
  CANCELED;
}
