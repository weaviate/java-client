package io.weaviate.client6.v1.api.collections.vectorindex;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record HFresh(
    @SerializedName("distance") Distance distance,
    @SerializedName("maxPostingSizeKB") Integer maxPostingSizeKb,
    @SerializedName("replicas") Integer replicaCount,
    @SerializedName("searchProbe") Integer searchProbe) implements VectorIndex {

  @Override
  public VectorIndex.Kind _kind() {
    return VectorIndex.Kind.HFRESH;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static HFresh of() {
    return of(ObjectBuilder.identity());
  }

  public static HFresh of(Function<Builder, ObjectBuilder<HFresh>> fn) {
    return fn.apply(new Builder()).build();
  }

  public HFresh(Builder builder) {
    this(
        builder.distance,
        builder.maxPostingSizeKb,
        builder.replicaCount,
        builder.searchProbe);
  }

  public static class Builder implements ObjectBuilder<HFresh> {
    private Distance distance;
    private Integer maxPostingSizeKb;
    private Integer replicaCount;
    private Integer searchProbe;

    public Builder distance(Distance distance) {
      this.distance = distance;
      return this;
    }

    public final Builder maxPostingSizeKb(int maxPostingSizeKb) {
      this.maxPostingSizeKb = maxPostingSizeKb;
      return this;
    }

    public final Builder replicaCount(int replicaCount) {
      this.replicaCount = replicaCount;
      return this;
    }

    public final Builder searchProbe(int searchProbe) {
      this.searchProbe = searchProbe;
      return this;
    }

    @Override
    public HFresh build() {
      return new HFresh(this);
    }
  }
}
