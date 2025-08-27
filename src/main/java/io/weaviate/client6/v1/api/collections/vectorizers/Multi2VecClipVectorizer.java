package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Multi2VecClipVectorizer(
    /** Base URL of the embedding service. */
    @SerializedName("inferenceUrl") String inferenceUrl,
    /** BLOB properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** TEXT properties included in the embedding. */
    @SerializedName("textFields") List<String> textFields,
    /** Weights of the included properties. */
    @SerializedName("weights") Weights weights,
    /** Vector index configuration. */
    VectorIndex vectorIndex) implements Vectorizer {

  private static record Weights(
      /**
       * Weights of the BLOB properties. Values appear in the same order as the
       * corresponding property names in {@code imageFields}.
       */
      @SerializedName("imageWeights") List<Float> imageWeights,
      /**
       * Weights of the TEXT properties. Values appear in the same order as the
       * corresponding property names in {@code textFields}.
       */
      @SerializedName("textWeights") List<Float> textWeights) {
  }

  @Override
  public Vectorizer.Kind _kind() {
    return Vectorizer.Kind.MULTI2VEC_CLIP;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Multi2VecClipVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Multi2VecClipVectorizer of(Function<Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Multi2VecClipVectorizer(Builder builder) {
    this(
        builder.inferenceUrl,
        builder.imageFields.keySet().stream().toList(),
        builder.textFields.keySet().stream().toList(),
        new Weights(
            builder.imageFields.values().stream().toList(),
            builder.textFields.values().stream().toList()),
        builder.vectorIndex);
  }

  public static class Builder implements ObjectBuilder<Multi2VecClipVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private String inferenceUrl;
    private Map<String, Float> imageFields = new HashMap<>();
    private Map<String, Float> textFields = new HashMap<>();

    /** Set base URL of the embedding service. */
    public Builder inferenceUrl(String inferenceUrl) {
      this.inferenceUrl = inferenceUrl;
      return this;
    }

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(List<String> fields) {
      fields.forEach(field -> imageFields.put(field, null));
      return this;
    }

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(String... fields) {
      return imageFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder imageField(String field, float weight) {
      imageFields.put(field, weight);
      return this;
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(List<String> fields) {
      fields.forEach(field -> textFields.put(field, null));
      return this;
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(String... fields) {
      return textFields(Arrays.asList(fields));
    }

    /**
     * Add TEXT property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder textField(String field, float weight) {
      textFields.put(field, weight);
      return this;
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
    public Multi2VecClipVectorizer build() {
      return new Multi2VecClipVectorizer(this);
    }
  }
}
