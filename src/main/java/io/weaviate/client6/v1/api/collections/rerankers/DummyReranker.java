package io.weaviate.client6.v1.api.collections.rerankers;

import io.weaviate.client6.v1.api.collections.Reranker;

public record DummyReranker() implements Reranker {

  @Override
  public Kind _kind() {
    return Reranker.Kind.DUMMY;
  }

  @Override
  public Object _self() {
    return this;
  }
}
