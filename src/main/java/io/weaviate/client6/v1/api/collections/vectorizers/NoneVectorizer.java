package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record NoneVectorizer(VectorIndex vectorIndex) implements Vectorizer {
  @Override
  public Kind _kind() {
    return Vectorizer.Kind.NONE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static NoneVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static NoneVectorizer of(Function<Builder, ObjectBuilder<NoneVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public NoneVectorizer(Builder builder) {
    this(builder.vectorIndex);
  }

  public static class Builder implements ObjectBuilder<NoneVectorizer> {
    private VectorIndex vectorIndex = Hnsw.of();

    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    @Override
    public NoneVectorizer build() {
      return new NoneVectorizer(this);
    }
  }
}
