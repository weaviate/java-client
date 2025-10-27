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

public record Text2VecGoogleAiStudioVectorizer(
    @SerializedName("model") String model,
    @SerializedName("titleProperty") String titleProperty,

    /** Properties included in the embedding. */
    @SerializedName("sourceProperties") List<String> sourceProperties,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.TEXT2VEC_GOOGLEAISTUDIO;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecGoogleAiStudioVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecGoogleAiStudioVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecGoogleAiStudioVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Text2VecGoogleAiStudioVectorizer(Builder builder) {
    this(
        builder.model,
        builder.titleProperty,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Text2VecGoogleAiStudioVectorizer> {
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private String model;
    private String titleProperty;

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder titleProperty(String titleProperty) {
      this.titleProperty = titleProperty;
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

    public Text2VecGoogleAiStudioVectorizer build() {
      return new Text2VecGoogleAiStudioVectorizer(this);
    }
  }
}
