package io.weaviate.client6.v1.api.collections.tenants;

import com.google.gson.annotations.SerializedName;

public enum TenantStatus {
  /** The tenant is activated and can be readily used. */
  @SerializedName(value = "ACTIVE", alternate = "HOT")
  ACTIVE,
  /**
   * Tenant needs to be activated before use.
   * Files are stored locally on the node.
   */
  @SerializedName(value = "INACTIVE", alternate = "COLD")
  INACTIVE,
  /**
   * Tenant is inactive and will need to be onloaded before use.
   * Files are stored in the configured remote (cloud) storage.
   */
  @SerializedName(value = "OFFLOADED", alternate = "FROZEN")
  OFFLOADED,

  /** Tenant is being offloaded to a remote storage. */
  @SerializedName(value = "OFFLOADING", alternate = "FREEZING")
  OFFLOADING,
  /** Tenant is being activated. */
  @SerializedName(value = "ONLOADING", alternate = "UNFREEZING")
  ONLOADING;
}
