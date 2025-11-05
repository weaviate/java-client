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

public record Text2VecTransformersVectorizer(
    @SerializedName("inferenceUrl") String baseUrl,
    @SerializedName("passageInferenceUrl") String passageInferenceUrl,
    @SerializedName("queryInferenceUrl") String queryInferenceUrl,
    @SerializedName("poolingStrategy") PoolingStrategy poolingStrategy,

    /** Properties included in the embedding. */
    @SerializedName("sourceProperties") List<String> sourceProperties,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.TEXT2VEC_TRANSFORMERS;
  }

  @Override
  public Object _self() {
    return this;
  }

  public enum PoolingStrategy {
    @SerializedName("MASKED_MEAN")
    MASKED_MEAN,
    @SerializedName("CLS")
    CLS;
  }

  public static Text2VecTransformersVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecTransformersVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecTransformersVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Text2VecTransformersVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.passageInferenceUrl,
        builder.queryInferenceUrl,
        builder.poolingStrategy,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Text2VecTransformersVectorizer> {
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private String baseUrl;
    private String passageInferenceUrl;
    private String queryInferenceUrl;
    private PoolingStrategy poolingStrategy;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder passageInferenceUrl(String passageInferenceUrl) {
      this.passageInferenceUrl = passageInferenceUrl;
      return this;
    }

    public Builder queryInferenceUrl(String queryInferenceUrl) {
      this.queryInferenceUrl = queryInferenceUrl;
      return this;
    }

    public Builder poolingStrategy(PoolingStrategy poolingStrategy) {
      this.poolingStrategy = poolingStrategy;
      return this;
    }

    /** Add properties to include in the embedding. */
    public Builder sourceProperties(String... properties) {
      return sourceProperties(Arrays.asList(properties));
    }

    /** Add properties to include in the embedding. */
    public Builder sourceProperties(List<String> properties) {
      this.sourceProperties.addAll(properties);
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

    public Text2VecTransformersVectorizer build() {
      return new Text2VecTransformersVectorizer(this);
    }
  }
}
