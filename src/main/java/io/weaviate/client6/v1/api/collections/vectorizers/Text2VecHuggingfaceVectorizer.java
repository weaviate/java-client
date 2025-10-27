package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Text2VecHuggingfaceVectorizer(
    @SerializedName("endpointURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("passageModel") String passageModel,
    @SerializedName("queryModel") String queryModel,
    @SerializedName("useCache") Boolean useCache,
    @SerializedName("useGPU") Boolean useGpu,
    @SerializedName("waitForModel") Boolean waitForModel,

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
    return VectorConfig.Kind.TEXT2VEC_HUGGINGFACE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecHuggingfaceVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecHuggingfaceVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecHuggingfaceVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecHuggingfaceVectorizer(
      String baseUrl,
      String model,
      String passageModel,
      String queryModel,
      Boolean useCache,
      Boolean useGpu,
      Boolean waitForModel,

      boolean vectorizeCollectionName,
      List<String> sourceProperties,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.model = model;
    this.passageModel = passageModel;
    this.queryModel = queryModel;
    this.useCache = useCache;
    this.useGpu = useGpu;
    this.waitForModel = waitForModel;

    this.vectorizeCollectionName = false;
    this.sourceProperties = Collections.emptyList();
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Text2VecHuggingfaceVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.passageModel,
        builder.queryModel,
        builder.useCache,
        builder.useGpu,
        builder.waitForModel,
        builder.vectorizeCollectionName,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Text2VecHuggingfaceVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private String baseUrl;
    private String model;
    private String passageModel;
    private String queryModel;
    private Boolean useCache;
    private Boolean useGpu;
    private Boolean waitForModel;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    /** The model to use for passage vectorization. */
    public Builder passageModel(String passageModel) {
      this.passageModel = passageModel;
      return this;
    }

    /** The model to use for query vectorization. */
    public Builder queryModel(String queryModel) {
      this.queryModel = queryModel;
      return this;
    }

    public Builder useCache(boolean useCache) {
      this.useCache = useCache;
      return this;
    }

    public Builder useGpu(boolean useGpu) {
      this.useGpu = useGpu;
      return this;
    }

    public Builder waitForModel(boolean waitForModel) {
      this.waitForModel = waitForModel;
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

    public Text2VecHuggingfaceVectorizer build() {
      return new Text2VecHuggingfaceVectorizer(this);
    }
  }
}
