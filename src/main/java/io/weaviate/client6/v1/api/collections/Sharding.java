package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Sharding(
    @SerializedName("virtualPerPhysical") Integer virtualPerPhysical,
    @SerializedName("desiredCound") Integer desiredCount,
    @SerializedName("desiredVirtualCount") Integer desiredVirtualCount) {

  public static Sharding of(Function<Builder, ObjectBuilder<Sharding>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Sharding(Builder builder) {
    this(
        builder.virtualPerPhysical,
        builder.desiredCount,
        builder.desiredVirtualCount);
  }

  public static class Builder implements ObjectBuilder<Sharding> {
    private Integer virtualPerPhysical;
    private Integer desiredCount;
    private Integer desiredVirtualCount;

    public Builder virtualPerPhysical(int virtualPerPhysical) {
      this.virtualPerPhysical = virtualPerPhysical;
      return this;
    }

    public Builder desiredCount(int desiredCount) {
      this.desiredCount = desiredCount;
      return this;
    }

    public Builder desiredVirtualCount(int desiredVirtualCount) {
      this.desiredVirtualCount = desiredVirtualCount;
      return this;
    }

    @Override
    public Sharding build() {
      return new Sharding(this);
    }
  }
}
