package io.weaviate.client6.v1.api.collections.vectorindex;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public class WrappedVectorIndex extends BaseVectorIndex {
  private final VectorIndex vectorIndex;

  public WrappedVectorIndex(Vectorizer vectorizer, VectorIndex vectorIndex) {
    super(vectorizer);
    this.vectorIndex = vectorIndex != null
        ? vectorIndex
        : VectorIndex.DEFAULT_VECTOR_INDEX.apply(vectorizer);
  }

  public WrappedVectorIndex(Vectorizer vectorizer) {
    this(vectorizer, null);
  }

  @Override
  public Kind _kind() {
    return this.vectorIndex._kind();
  }

  @Override
  public Object config() {
    return this.vectorIndex.config();
  }

  @Override
  public Vectorizer vectorizer() {
    return super.vectorizer;
  }

  public static abstract class Builder<SELF extends Builder<SELF, V>, V extends Vectorizer>
      implements ObjectBuilder<V> {
    private VectorIndex vectorIndex;

    @SuppressWarnings("unchecked")
    public SELF vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return (SELF) this;
    }

    public VectorIndex buildVectorIndex() {
      var vectorizer = build();
      return new WrappedVectorIndex(vectorizer, vectorIndex);
    }

    public static <B extends Builder<B, V>, V extends Vectorizer> Function<B, Builder<B, V>> identity() {
      return builder -> builder;
    }
  }
}
