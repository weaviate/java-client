package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Text2VecContextionaryVectorizer(
    @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
    VectorIndex vectorIndex) implements Vectorizer {

  @Override
  public Vectorizer.Kind _kind() {
    return Vectorizer.Kind.TEXT2VEC_CONTEXTIONARY;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecContextionaryVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecContextionaryVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecContextionaryVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Text2VecContextionaryVectorizer(Builder builder) {
    this(builder.vectorizeCollectionName, builder.vectorIndex);
  }

  public static class Builder implements ObjectBuilder<Text2VecContextionaryVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private boolean vectorizeCollectionName = false;

    public Builder vectorizeCollectionName(boolean enable) {
      this.vectorizeCollectionName = enable;
      return this;
    }

    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    public Text2VecContextionaryVectorizer build() {
      return new Text2VecContextionaryVectorizer(this);
    }
  }
}
