package io.weaviate.client6.v1.api.collections.vectorindex;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Flat(@SerializedName("vectorCacheMaxObjects") Long vectorCacheMaxObjects)
    implements VectorIndex {

  @Override
  public VectorIndex.Kind _kind() {
    return VectorIndex.Kind.FLAT;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Flat of() {
    return of(ObjectBuilder.identity());
  }

  public static Flat of(Function<Builder, ObjectBuilder<Flat>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Flat(Builder builder) {
    this(builder.vectorCacheMaxObjects);
  }

  public static class Builder implements ObjectBuilder<Flat> {

    private Long vectorCacheMaxObjects;

    public Builder vectorCacheMaxObjects(long vectorCacheMaxObjects) {
      this.vectorCacheMaxObjects = vectorCacheMaxObjects;
      return this;
    }

    @Override
    public Flat build() {
      return new Flat(this);
    }
  }
}
