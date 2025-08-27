package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record SelfProvidedVectorizer(VectorIndex vectorIndex) implements Vectorizer {
  @Override
  public Kind _kind() {
    return Vectorizer.Kind.NONE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static SelfProvidedVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static SelfProvidedVectorizer of(Function<Builder, ObjectBuilder<SelfProvidedVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public SelfProvidedVectorizer(Builder builder) {
    this(builder.vectorIndex);
  }

  public static class Builder implements ObjectBuilder<SelfProvidedVectorizer> {
    private VectorIndex vectorIndex = Hnsw.of();

    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    @Override
    public SelfProvidedVectorizer build() {
      return new SelfProvidedVectorizer(this);
    }
  }
}
