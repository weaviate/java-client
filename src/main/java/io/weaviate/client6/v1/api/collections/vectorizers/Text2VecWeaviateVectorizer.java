package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Text2VecWeaviateVectorizer(
    /** Weaviate Embeddings Service base URL. */
    @SerializedName("baseURL") String baseUrl,
    /** Dimensionality of the generated vectors. */
    @SerializedName("dimensions") Integer dimensions,
    /** Embedding model. */
    @SerializedName("model") String model,
    /** Properties included in the embedding. */
    @SerializedName("properties") List<String> sourceProperties,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.TEXT2VEC_WEAVIATE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecWeaviateVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecWeaviateVectorizer of(Function<Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Text2VecWeaviateVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.dimensions,
        builder.model,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static final String SNOWFLAKE_ARCTIC_EMBED_M_15 = "Snowflake/snowflake-arctic-embed-m-v1.5";
  public static final String SNOWFLAKE_ARCTIC_EMBED_L_20 = "Snowflake/snowflake-arctic-embed-l-v2.0";

  public static class Builder implements ObjectBuilder<Text2VecWeaviateVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;
    private String baseUrl;
    private Integer dimensions;
    private String model;
    private List<String> sourceProperties;

    /**
     * Base URL for Weaviate Embeddings Service. This can be omitted when connecting
     * to a Weaviate Cloud instance: the client will automatically set the necessary
     * headers.
     */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Set target dimensionality for generated embeddings. */
    public Builder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    /**
     * Select the embedding model.
     *
     * @see Text2VecWeaviateVectorizer#SNOWFLAKE_ARCTIC_EMBED_M_15
     * @see Text2VecWeaviateVectorizer#SNOWFLAKE_ARCTIC_EMBED_L_20
     */
    public Builder model(String model) {
      this.model = model;
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

    public Text2VecWeaviateVectorizer build() {
      return new Text2VecWeaviateVectorizer(this);
    }
  }
}
