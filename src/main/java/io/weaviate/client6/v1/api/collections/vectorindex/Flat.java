package io.weaviate.client6.v1.api.collections.vectorindex;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Flat(
    Vectorizer vectorizer,
    @SerializedName("vectorCacheMaxObjects") Long vectorCacheMaxObjects) implements VectorIndex {

  @Override
  public Kind type() {
    return VectorIndex.Kind.FLAT;
  }

  @Override
  public Object config() {
    return new Flat(
        null,
        this.vectorCacheMaxObjects);
  }

  public static Flat of(Vectorizer vectorizer) {
    return of(vectorizer, ObjectBuilder.identity());
  }

  public static Flat of(Vectorizer vectorizer, Function<Builder, ObjectBuilder<Flat>> fn) {
    return fn.apply(new Builder(vectorizer)).build();
  }

  public Flat(Builder builder) {
    this(builder.vectorizer, builder.vectorCacheMaxObjects);
  }

  public static class Builder implements ObjectBuilder<Flat> {
    // Required parameters.
    private final Vectorizer vectorizer;

    private Long vectorCacheMaxObjects;

    protected Builder(Vectorizer vectorizer) {
      this.vectorizer = vectorizer;
    }

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
