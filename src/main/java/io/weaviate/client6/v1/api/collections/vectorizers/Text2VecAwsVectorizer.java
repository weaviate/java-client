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

public record Text2VecAwsVectorizer(
    @SerializedName("endpoint") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("region") String region,
    @SerializedName("service") Service service,

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
    return VectorConfig.Kind.TEXT2VEC_AWS;
  }

  @Override
  public Object _self() {
    return this;
  }

  public enum Service {
    @SerializedName("BEDROCK")
    BEDROCK,
    @SerializedName("SAGEMAKER")
    SAGEMAKER;
  }

  public static final String AMAZON_TITAN_EMBED_TEXT_V1 = "amazon.titan-embed-text-v1";
  public static final String COHERE_EMBED_ENGLISH_V3 = "cohere.embed-english-v3";
  public static final String COHERE_EMBED_MULTILINGUAL_V3 = "cohere.embed-multilingual-v3";

  public static Text2VecAwsVectorizer bedrock(String model) {
    return bedrock(model, ObjectBuilder.identity());
  }

  public static Text2VecAwsVectorizer bedrock(
      String model,
      Function<Builder, ObjectBuilder<Text2VecAwsVectorizer>> fn) {
    return fn.apply(new BedrockBuilder(model)).build();
  }

  public static Text2VecAwsVectorizer sagemaker(String baseUrl) {
    return sagemaker(baseUrl, ObjectBuilder.identity());
  }

  public static Text2VecAwsVectorizer sagemaker(
      String baseUrl,
      Function<Builder, ObjectBuilder<Text2VecAwsVectorizer>> fn) {
    return fn.apply(new SagemakerBuilder(baseUrl)).build();
  }

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecAwsVectorizer(
      String baseUrl,
      String model,
      String region,
      Service service,

      boolean vectorizeCollectionName,
      List<String> sourceProperties,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.model = model;
    this.region = region;
    this.service = service;

    this.vectorizeCollectionName = false;
    this.sourceProperties = sourceProperties;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Text2VecAwsVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.region,
        builder.service,

        builder.vectorizeCollectionName,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public abstract static class Builder implements ObjectBuilder<Text2VecAwsVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private final Service service;
    private String baseUrl;
    private String model;
    private String region;

    protected Builder(Service service) {
      this.service = service;
    }

    /** Required for {@link Service#SAGEMAKER}. */
    protected Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Required for {@link Service#BEDROCK}. */
    protected Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder region(String region) {
      this.region = region;
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

    public Text2VecAwsVectorizer build() {
      return new Text2VecAwsVectorizer(this);
    }
  }

  public static class BedrockBuilder extends Builder {
    public BedrockBuilder(String model) {
      super(Service.BEDROCK);
      super.model(model);
    }

    @Override
    /** Required for {@link Service#BEDROCK}. */
    public Builder model(String model) {
      return super.model(model);
    }
  }

  public static class SagemakerBuilder extends Builder {
    public SagemakerBuilder(String baseUrl) {
      super(Service.SAGEMAKER);
      super.baseUrl(baseUrl);
    }

    /** Required for {@link Service#SAGEMAKER}. */
    protected Builder baseUrl(String baseUrl) {
      return super.baseUrl(baseUrl);
    }
  }
}
