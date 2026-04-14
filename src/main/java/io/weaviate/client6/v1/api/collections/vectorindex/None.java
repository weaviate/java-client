package io.weaviate.client6.v1.api.collections.vectorindex;

import io.weaviate.client6.v1.api.collections.VectorIndex;

public record None() implements VectorIndex {
  @Override
  public VectorIndex.Kind _kind() {
    return VectorIndex.Kind.NONE;
  }

  @Override
  public Object _self() {
    return this;
  }
}
