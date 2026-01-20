package io.weaviate.client6.v1.api.collections.tenants;

import com.google.gson.annotations.SerializedName;

public record Tenant(
    @SerializedName("name") String name,
    @SerializedName("activityStatus") TenantStatus status) {

  /** Create new tenant with {@link TenantStatus#ACTIVE}. */
  public static Tenant active(String name) {
    return new Tenant(name, TenantStatus.ACTIVE);
  }

  /** Create new tenant with {@link TenantStatus#INACTIVE}. */
  public static Tenant inactive(String name) {
    return new Tenant(name, TenantStatus.INACTIVE);
  }

  /** Returns true if tenant's status is {@link TenantStatus#ACTIVE}. */
  public boolean isActive() {
    return status == TenantStatus.ACTIVE;
  }

  /** Returns true if tenant's status is {@link TenantStatus#INACTIVE}. */
  public boolean isInactive() {
    return status == TenantStatus.INACTIVE;
  }

  /** Returns true if tenant's status is {@link TenantStatus#OFFLOADED}. */
  public boolean isOffloaded() {
    return status == TenantStatus.OFFLOADED;
  }

  /** Returns true if tenant's status is {@link TenantStatus#OFFLOADING}. */
  public boolean isOffloading() {
    return status == TenantStatus.OFFLOADING;
  }

  /** Returns true if tenant's status is {@link TenantStatus#ONLOADING}. */
  public boolean isOnloading() {
    return status == TenantStatus.ONLOADING;
  }
}
