package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Text2VecContextionaryVectorizer(
    /**
     * Weaviate defaults to {@code true} if the value is not provided.
     * Because text2vec-contextionary cannot handle understores in collection names,
     * this quickly becomes inconvenient.
     *
     * To avoid that we send "vectorizeClassName": false all the time
     * and make it impossible to enable this feature, as it is deprecated.
     */
    @Deprecated @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
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

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecContextionaryVectorizer(boolean vectorizeCollectionName, VectorIndex vectorIndex) {
    this.vectorizeCollectionName = false;
    this.vectorIndex = vectorIndex;
  }

  public Text2VecContextionaryVectorizer(Builder builder) {
    this(builder.vectorizeCollectionName, builder.vectorIndex);
  }

  public static class Builder implements ObjectBuilder<Text2VecContextionaryVectorizer> {
    private final boolean vectorizeCollectionName = false;

    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    public Text2VecContextionaryVectorizer build() {
      return new Text2VecContextionaryVectorizer(this);
    }
  }
}
