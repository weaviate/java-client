package io.weaviate.client6.v1.api.collections.vectorindex;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
abstract class BaseVectorIndex implements VectorIndex {
  protected final Vectorizer vectorizer;

  @Override
  public Vectorizer vectorizer() {
    return this.vectorizer;
  }

  public BaseVectorIndex(Vectorizer vectorizer) {
    this.vectorizer = vectorizer;
  }
}
