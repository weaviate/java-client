package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Img2VecNeuralVectorizer(
    /** BLOB properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** Vector index configuration. */
    VectorIndex vectorIndex) implements Vectorizer {

  @Override
  public Vectorizer.Kind _kind() {
    return Vectorizer.Kind.IMG2VEC_NEURAL;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Img2VecNeuralVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Img2VecNeuralVectorizer of(Function<Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Img2VecNeuralVectorizer(Builder builder) {
    this(builder.imageFields, builder.vectorIndex);
  }

  public static class Builder implements ObjectBuilder<Img2VecNeuralVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private List<String> imageFields = new ArrayList<>();

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(List<String> fields) {
      this.imageFields = fields;
      return this;
    }

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(String... fields) {
      return imageFields(Arrays.asList(fields));
    }

    /**
     * Override default vector index configuration.
     *
     * <a href=
     * "https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index#hnsw-index-parameters">HNSW</a>
     * is the default vector index.
     */
    public Builder vectorIndex(Function<Hnsw.Builder, ObjectBuilder<Hnsw>> fn) {
      return vectorIndex(VectorIndex.createDefault(fn));
    }

    /**
     * Override default vector index configuration.
     *
     * <a href=
     * "https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index#hnsw-index-parameters">HNSW</a>
     * is the default vector index.
     */
    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    @Override
    public Img2VecNeuralVectorizer build() {
      return new Img2VecNeuralVectorizer(this);
    }
  }
}
