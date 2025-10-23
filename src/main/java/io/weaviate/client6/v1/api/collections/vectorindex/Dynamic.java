package io.weaviate.client6.v1.api.collections.vectorindex;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Dynamic(
    @SerializedName("hnsw") Hnsw hnsw,
    @SerializedName("flat") Flat flat,
    @SerializedName("threshold") Long threshold)
    implements VectorIndex {

  @Override
  public VectorIndex.Kind _kind() {
    return VectorIndex.Kind.DYNAMIC;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Dynamic of() {
    return of(ObjectBuilder.identity());
  }

  public static Dynamic of(Function<Builder, ObjectBuilder<Dynamic>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Dynamic(Builder builder) {
    this(
        builder.hnsw,
        builder.flat,
        builder.threshold);
  }

  public static class Builder implements ObjectBuilder<Dynamic> {

    private Hnsw hnsw;
    private Flat flat;
    private Long threshold;

    public Builder hnsw(Hnsw hnsw) {
      this.hnsw = hnsw;
      return this;
    }

    public Builder flat(Flat flat) {
      this.flat = flat;
      return this;
    }

    public Builder threshold(long threshold) {
      this.threshold = threshold;
      return this;
    }

    @Override
    public Dynamic build() {
      return new Dynamic(this);
    }
  }
}
