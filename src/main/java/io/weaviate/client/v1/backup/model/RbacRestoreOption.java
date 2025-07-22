package io.weaviate.client.v1.backup.model;

import com.google.gson.annotations.SerializedName;

public enum RbacRestoreOption {
  @SerializedName("noRestore")
  NO_RESTORE,
  @SerializedName("all")
  ALL;
}
