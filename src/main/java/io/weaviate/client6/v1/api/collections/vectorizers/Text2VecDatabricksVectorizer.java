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

public record Text2VecDatabricksVectorizer(
    @SerializedName("endpoint") String baseUrl,
    @SerializedName("instruction") String instruction,

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
    return VectorConfig.Kind.TEXT2VEC_DATABRICKS;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecDatabricksVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecDatabricksVectorizer of(
      Function<Builder, ObjectBuilder<Text2VecDatabricksVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecDatabricksVectorizer(
      String baseUrl,
      String instruction,

      boolean vectorizeCollectionName,
      List<String> sourceProperties,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.instruction = instruction;

    this.vectorizeCollectionName = false;
    this.sourceProperties = sourceProperties;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Text2VecDatabricksVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.instruction,

        builder.vectorizeCollectionName,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Text2VecDatabricksVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    private String baseUrl;
    private String instruction;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder instruction(String instruction) {
      this.instruction = instruction;
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

    public Text2VecDatabricksVectorizer build() {
      return new Text2VecDatabricksVectorizer(this);
    }
  }
}
