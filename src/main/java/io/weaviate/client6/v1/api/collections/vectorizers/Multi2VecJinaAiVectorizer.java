package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Multi2VecJinaAiVectorizer(
    /** Base URL of the embedding service. */
    @SerializedName("baseURL") String baseUrl,
    /** Inference model to use. */
    @SerializedName("model") String model,
    @SerializedName("dimensions") Integer dimensions,
    /** BLOB properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** TEXT properties included in the embedding. */
    @SerializedName("textFields") List<String> textFields,
    /** Weights of the included properties. */
    @SerializedName("weights") Weights weights,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

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
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.MULTI2VEC_JINAAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Multi2VecJinaAiVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Multi2VecJinaAiVectorizer of(Function<Builder, ObjectBuilder<Multi2VecJinaAiVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Multi2VecJinaAiVectorizer(
      String baseUrl,
      String model,
      Integer dimensions,
      List<String> imageFields,
      List<String> textFields,
      Weights weights,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.model = model;
    this.dimensions = dimensions;
    this.imageFields = imageFields;
    this.textFields = textFields;
    this.weights = weights;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Multi2VecJinaAiVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.dimensions,
        builder.imageFields,
        builder.textFields,
        builder.getWeights(),
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Multi2VecJinaAiVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;

    private List<String> imageFields;
    private List<Float> imageWeights;
    private List<String> textFields;
    private List<Float> textWeights;

    private String baseUrl;
    private String model;
    private Integer dimensions;

    /** Set base URL of the embedding service. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

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
     * Add BLOB property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder imageField(String field, float weight) {
      if (this.imageFields == null) {
        this.imageFields = new ArrayList<>();
      }
      if (this.imageWeights == null) {
        this.imageWeights = new ArrayList<>();
      }
      this.imageFields.add(field);
      this.imageWeights.add(weight);
      return this;
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(List<String> fields) {
      this.textFields = fields;
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
      if (this.textFields == null) {
        this.textFields = new ArrayList<>();
      }
      if (this.textWeights == null) {
        this.textWeights = new ArrayList<>();
      }
      this.textFields.add(field);
      this.textWeights.add(weight);
      return this;
    }

    protected Weights getWeights() {
      if (this.textWeights != null || this.imageWeights != null) {
        return new Weights(this.imageWeights, this.textWeights);
      }
      return null;
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

    public Builder quantization(Quantization quantization) {
      this.quantization = quantization;
      return this;
    }

    @Override
    public Multi2VecJinaAiVectorizer build() {
      return new Multi2VecJinaAiVectorizer(this);
    }
  }
}
