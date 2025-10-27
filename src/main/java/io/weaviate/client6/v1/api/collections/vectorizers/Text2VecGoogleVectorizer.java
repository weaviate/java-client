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

public record Text2VecGoogleVectorizer(
    @SerializedName("apiEndpoint") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("titleProperty") String titleProperty,
    @SerializedName("dimensions") Integer dimensions,
    @SerializedName("projectId") String projectId,

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
    return VectorConfig.Kind.TEXT2VEC_GOOGLE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecGoogleVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecGoogleVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecGoogleVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecGoogleVectorizer(
      String baseUrl,
      String model,
      String titleProperty,
      Integer dimensions,
      String projectId,

      boolean vectorizeCollectionName,
      List<String> sourceProperties,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.model = model;
    this.titleProperty = titleProperty;
    this.dimensions = dimensions;
    this.projectId = projectId;

    this.vectorizeCollectionName = false;
    this.sourceProperties = sourceProperties;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Text2VecGoogleVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.titleProperty,
        builder.dimensions,
        builder.projectId,

        builder.vectorizeCollectionName,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Text2VecGoogleVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private String baseUrl;
    private String model;
    private String titleProperty;
    private Integer dimensions;
    private String projectId;

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

    public Builder titleProperty(String titleProperty) {
      this.titleProperty = titleProperty;
      return this;
    }

    public Builder projectId(String projectId) {
      this.projectId = projectId;
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

    public Text2VecGoogleVectorizer build() {
      return new Text2VecGoogleVectorizer(this);
    }
  }
}
