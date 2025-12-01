package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Text2VecAzureOpenAiVectorizer(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("dimensions") Integer dimensions,
    @SerializedName("model") String model,
    @SerializedName("deploymentId") String deploymentId,
    @SerializedName("resourceName") String resourceName,

    /**
     * Weaviate defaults to {@code true} if the value is not provided.
     * To avoid that we send "vectorizeClassName": false all the time
     * and make it impossible to enable this feature, as it is deprecated.
     */
    @Deprecated @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
    /** Properties included in the embedding. */
    @SerializedName("properties") List<String> sourceProperties,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.TEXT2VEC_AZURE_OPENAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecAzureOpenAiVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecAzureOpenAiVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecAzureOpenAiVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecAzureOpenAiVectorizer(
      String baseUrl,
      Integer dimensions,
      String model,
      String deploymentId,
      String resourceName,

      boolean vectorizeCollectionName,
      List<String> sourceProperties,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.dimensions = dimensions;
    this.model = model;
    this.deploymentId = deploymentId;
    this.resourceName = resourceName;

    this.vectorizeCollectionName = false;
    this.sourceProperties = sourceProperties;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Text2VecAzureOpenAiVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.dimensions,
        builder.model,
        builder.deploymentId,
        builder.resourceName,

        builder.vectorizeCollectionName,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Text2VecAzureOpenAiVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private Quantization quantization;
    private List<String> sourceProperties;
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private String baseUrl;
    private Integer dimensions;
    private String model;
    private String deploymentId;
    private String resourceName;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Set the number of dimensions for the vector embeddings. */
    public Builder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    /** Select the embedding model. */
    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder deploymentId(String deploymentId) {
      this.deploymentId = deploymentId;
      return this;
    }

    public Builder resourceName(String resourceName) {
      this.resourceName = resourceName;
      return this;
    }

    /** Add properties to include in the embedding. */
    public Builder sourceProperties(String... properties) {
      return sourceProperties(Arrays.asList(properties));
    }

    /** Add properties to include in the embedding. */
    public Builder sourceProperties(List<String> properties) {
      this.sourceProperties = properties;
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

    public Text2VecAzureOpenAiVectorizer build() {
      return new Text2VecAzureOpenAiVectorizer(this);
    }
  }
}
