package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record MultiTenancy(
    /** Is multi-tenancy enabled for this collection. */
    @SerializedName("enabled") boolean enabled,
    /** Is auto tenant creation enabled for this collection. */
    @SerializedName("autoTenantCreation") Boolean createAutomatically,
    /** Is auto tenant activation enabled for this collection. */
    @SerializedName("autoTenantActivation") Boolean activateAutomatically) {

  public static MultiTenancy of(Function<Builder, ObjectBuilder<MultiTenancy>> fn) {
    return fn.apply(new Builder()).build();
  }

  public MultiTenancy(Builder builder) {
    this(
        builder.enabled,
        builder.createAutomatically,
        builder.activateAutomatically);
  }

  public static class Builder implements ObjectBuilder<MultiTenancy> {
    private boolean enabled = true;
    private Boolean createAutomatically;
    private Boolean activateAutomatically;

    /** Enable / disable multi-tenancy for this collection. */
    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    /** Enable / disable auto tenant creation. */
    public Builder autoTenantCreation(boolean enabled) {
      this.createAutomatically = enabled;
      return this;
    }

    /** Enable / disable auto tenant activation. */
    public Builder autoTenantActivation(boolean enabled) {
      this.activateAutomatically = enabled;
      return this;
    }

    @Override
    public MultiTenancy build() {
      return new MultiTenancy(this);
    }
  }
}
