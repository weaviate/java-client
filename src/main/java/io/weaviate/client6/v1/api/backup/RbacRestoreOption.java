package io.weaviate.client6.v1.api.backup;

import com.google.gson.annotations.SerializedName;

/** Controls which RBAC objects (users, roles) get restored. */
public enum RbacRestoreOption {
  /** Do not restore any objects. */
  @SerializedName("noRestore")
  NONE,
  /** Restore all objects. */
  @SerializedName("all")
  ALL;
}
