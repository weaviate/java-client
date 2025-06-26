package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Replication(
    @SerializedName("factor") Integer replicationFactor,
    @SerializedName("asyncEnabled") Boolean asyncEnabled,
    @SerializedName("deletionStrategy") DeletionStrategy deletionStrategy) {

  public static enum DeletionStrategy {
    @SerializedName("NoAutomatedResolution")
    NO_AUTOMATED_RESOLUTION,
    @SerializedName("DeleteOnConflict")
    DELETE_ON_CONFLICT,
    @SerializedName("TimeBasedResolution")
    TIME_BASED_RESOLUTION;
  }

  public static Replication of(Function<Builder, ObjectBuilder<Replication>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Replication(Builder builder) {
    this(
        builder.replicationFactor,
        builder.asyncEnabled,
        builder.deletionStrategy);
  }

  public static class Builder implements ObjectBuilder<Replication> {
    private Integer replicationFactor;
    private Boolean asyncEnabled;
    private DeletionStrategy deletionStrategy;

    public Builder replicationFactor(int replicationFactor) {
      this.replicationFactor = replicationFactor;
      return this;
    }

    public Builder asyncEnabled(boolean asyncEnabled) {
      this.asyncEnabled = asyncEnabled;
      return this;
    }

    public Builder deletionStrategy(DeletionStrategy deletionStrategy) {
      this.deletionStrategy = deletionStrategy;
      return this;
    }

    @Override
    public Replication build() {
      return new Replication(this);
    }
  }
}
