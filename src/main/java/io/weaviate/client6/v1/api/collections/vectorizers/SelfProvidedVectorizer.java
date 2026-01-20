package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record SelfProvidedVectorizer(
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {
  @Override
  public Kind _kind() {
    return VectorConfig.Kind.NONE;
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
    this(builder.vectorIndex, builder.quantization);
  }

  public static class Builder implements ObjectBuilder<SelfProvidedVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;

    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    public Builder quantization(Quantization quantization) {
      this.quantization = quantization;
      return this;
    }

    @Override
    public SelfProvidedVectorizer build() {
      return new SelfProvidedVectorizer(this);
    }
  }
}
