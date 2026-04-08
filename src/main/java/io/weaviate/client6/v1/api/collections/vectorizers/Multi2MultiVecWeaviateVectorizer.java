package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Multi2MultiVecWeaviateVectorizer(
    /** Base URL of the embedding service. */
    @SerializedName("baseURL") String baseUrl,
    /** Inference model to use. */
    @SerializedName("model") String model,
    /** BLOB properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** TEXT properties included in the embedding. */
    @SerializedName("textFields") List<String> textFields,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.MULTI2MULTIVEC_WEAVIATE;
  }

  @Override
  public Object _self() {
    return this;
  }

  /** ColBERT-family model available in the Weaviate Embedding Service. */
  public static String MODERNVBERT_COLMODERNVBERT = "ModernVBERT/colmodernvbert";

  public static Multi2MultiVecWeaviateVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Multi2MultiVecWeaviateVectorizer of(
      Function<Builder, ObjectBuilder<Multi2MultiVecWeaviateVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Multi2MultiVecWeaviateVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.imageFields,
        builder.textFields,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Multi2MultiVecWeaviateVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;

    private String baseUrl;
    private String model;

    private List<String> imageFields;
    private List<String> textFields;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(List<String> fields) {
      imageFields = fields;
      return this;
    }

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(String... fields) {
      return imageFields(Arrays.asList(fields));
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(List<String> fields) {
      textFields = fields;
      return this;
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(String... fields) {
      return textFields(Arrays.asList(fields));
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
    public Multi2MultiVecWeaviateVectorizer build() {
      return new Multi2MultiVecWeaviateVectorizer(this);
    }
  }
}
