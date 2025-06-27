package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record MultiTenancy(
    @SerializedName("autoTenantCreation") Boolean createAutomatically,
    @SerializedName("autoTenantActivate") Boolean activateAutomatically) {

  public static MultiTenancy of(Function<Builder, ObjectBuilder<MultiTenancy>> fn) {
    return fn.apply(new Builder()).build();
  }

  public MultiTenancy(Builder builder) {
    this(
        builder.createAutomatically,
        builder.activateAutomatically);
  }

  public static class Builder implements ObjectBuilder<MultiTenancy> {
    private Boolean createAutomatically;
    private Boolean activateAutomatically;

    public Builder createAutomatically(boolean createAutomatically) {
      this.createAutomatically = createAutomatically;
      return this;
    }

    public Builder activateAutomatically(boolean activateAutomatically) {
      this.activateAutomatically = activateAutomatically;
      return this;
    }

    @Override
    public MultiTenancy build() {
      return new MultiTenancy(this);
    }
  }
}
