package io.weaviate.client6.v1.api.backup;

import com.google.gson.annotations.SerializedName;

public enum RbacRestoreOption {
  @SerializedName("noRestore")
  NONE,
  @SerializedName("all")
  ALL;
}
