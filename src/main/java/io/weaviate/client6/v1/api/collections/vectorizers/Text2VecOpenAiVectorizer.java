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

public record Text2VecOpenAiVectorizer(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("modelVersion") String modelVersion,
    @SerializedName("dimensions") Integer dimensions,
    @SerializedName("type") ModelType modelType,

    /**
     * Weaviate defaults to {@code true} if the value is not provided.
     * To avoid that we send "vectorizeClassName": false all the time
     * and make it impossible to enable this feature, as it is deprecated.
     */
    @Deprecated @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
    /** Properties included in the embedding. */
    @SerializedName("sourceProperties") List<String> sourceProperties,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.TEXT2VEC_OPENAI;
  }

  public static String TEXT_EMBEDDING_3_SMALL = "text-embeding-3-small";
  public static String TEXT_EMBEDDING_3_LARGE = "text-embeding-3-large";
  public static String TEXT_EMBEDDING_ADA_002 = "text-embeding-ada-002";

  @Override
  public Object _self() {
    return this;
  }

  public enum ModelType {
    @SerializedName("CODE")
    CODE,
    @SerializedName("TEXT")
    TEXT;
  }

  public static Text2VecOpenAiVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecOpenAiVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecOpenAiVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecOpenAiVectorizer(
      String baseUrl,
      String model,
      String modelVersion,
      Integer dimensions,
      ModelType modelType,

      boolean vectorizeCollectionName,
      List<String> sourceProperties,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.model = model;
    this.modelVersion = modelVersion;
    this.dimensions = dimensions;
    this.modelType = modelType;

    this.vectorizeCollectionName = false;
    this.sourceProperties = sourceProperties;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Text2VecOpenAiVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.modelVersion,
        builder.dimensions,
        builder.modelType,

        builder.vectorizeCollectionName,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Text2VecOpenAiVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private String baseUrl;
    private String model;
    private String modelVersion;
    private Integer dimensions;
    private ModelType modelType;

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

    public Builder modelVersion(String modelVersion) {
      this.modelVersion = modelVersion;
      return this;
    }

    public Builder modelType(ModelType modelType) {
      this.modelType = modelType;
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

    public Text2VecOpenAiVectorizer build() {
      return new Text2VecOpenAiVectorizer(this);
    }
  }
}
