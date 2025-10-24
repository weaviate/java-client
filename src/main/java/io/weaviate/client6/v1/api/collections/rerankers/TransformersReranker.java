package io.weaviate.client6.v1.api.collections.rerankers;

import io.weaviate.client6.v1.api.collections.Reranker;

public record TransformersReranker() implements Reranker {

  @Override
  public Kind _kind() {
    return Reranker.Kind.NVIDIA;
  }

  @Override
  public Object _self() {
    return this;
  }
}
