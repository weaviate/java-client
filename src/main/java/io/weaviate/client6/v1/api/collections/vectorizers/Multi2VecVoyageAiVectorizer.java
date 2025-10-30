package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Multi2VecVoyageAiVectorizer(
    /** Base URL of the embedding service. */
    @SerializedName("baseURL") String baseUrl,
    /** Inference model to use. */
    @SerializedName("model") String model,
    @SerializedName("outputEncoding") String outputEncoding,
    @SerializedName("truncate") Boolean truncate,
    /** BLOB properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** TEXT properties included in the embedding. */
    @SerializedName("textFields") List<String> textFields,
    /** Weights of the included properties. */
    @SerializedName("weights") Weights weights,
    /**
     * Weaviate defaults to {@code true} if the value is not provided.
     * To avoid that we send "vectorizeClassName": false all the time
     * and make it impossible to enable this feature, as it is deprecated.
     */
    @Deprecated @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
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
    return VectorConfig.Kind.MULTI2VEC_VOYAGEAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Multi2VecVoyageAiVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Multi2VecVoyageAiVectorizer of(Function<Builder, ObjectBuilder<Multi2VecVoyageAiVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Multi2VecVoyageAiVectorizer(
      String baseUrl,
      String model,
      String outputEncoding,
      Boolean truncate,
      List<String> imageFields,
      List<String> textFields,
      Weights weights,
      boolean vectorizeCollectionName,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.vectorizeCollectionName = false;
    this.baseUrl = baseUrl;
    this.model = model;
    this.outputEncoding = outputEncoding;
    this.truncate = truncate;
    this.imageFields = imageFields;
    this.textFields = textFields;
    this.weights = weights;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Multi2VecVoyageAiVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.outputEncoding,
        builder.truncate,
        builder.imageFields.keySet().stream().toList(),
        builder.textFields.keySet().stream().toList(),
        new Weights(
            builder.imageFields.values().stream().toList(),
            builder.textFields.values().stream().toList()),
        builder.vectorizeCollectionName,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Multi2VecVoyageAiVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;

    private Map<String, Float> imageFields = new LinkedHashMap<>();
    private Map<String, Float> textFields = new LinkedHashMap<>();

    private String baseUrl;
    private String model;
    private String outputEncoding;
    private Boolean truncate;

    /** Set base URL of the embedding service. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder outputEncoding(String outputEncoding) {
      this.outputEncoding = outputEncoding;
      return this;
    }

    public Builder truncate(boolean truncate) {
      this.truncate = truncate;
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

    public Builder quantization(Quantization quantization) {
      this.quantization = quantization;
      return this;
    }

    @Override
    public Multi2VecVoyageAiVectorizer build() {
      return new Multi2VecVoyageAiVectorizer(this);
    }
  }
}
