package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Multi2VecGoogleVectorizer(
    @SerializedName("projectId") String projectId,
    @SerializedName("model") String model,
    @SerializedName("dimensions") Integer dimensions,
    @SerializedName("location") String location,
    @SerializedName("videoIntervalSeconds") Integer videoIntervalSeconds,
    /** BLOB image properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** BLOB video properties included in the embedding. */
    @SerializedName("videoFields") List<String> videoFields,
    /** TEXT properties included in the embedding. */
    @SerializedName("textFields") List<String> textFields,
    /** Weights of the included properties. */
    @SerializedName("weights") Weights weights,
    /**
     * Weaviate defaults to {@code true} if the value is not provided.
     * To avoid that we send "vectorizeClassName": false all the time
     * and make it impossible to enable this feature, as it is deprecated.
     */
    @Deprecated @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  private static record Weights(
      /**
       * Weights of the BLOB image properties. Values appear in the same order as the
       * corresponding property names in {@code imageFields}.
       */
      @SerializedName("imageWeights") List<Float> imageWeights,
      /**
       * Weights of the BLOB video properties. Values appear in the same order as the
       * corresponding property names in {@code videoFields}.
       */
      @SerializedName("videoWeights") List<Float> videoWeights,
      /**
       * Weights of the TEXT properties. Values appear in the same order as the
       * corresponding property names in {@code textFields}.
       */
      @SerializedName("textWeights") List<Float> textWeights) {
  }

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.MULTI2VEC_GOOGLE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Multi2VecGoogleVectorizer of(String location) {
    return of(location, ObjectBuilder.identity());
  }

  public static Multi2VecGoogleVectorizer of(
      String location,
      Function<Builder, ObjectBuilder<Multi2VecGoogleVectorizer>> fn) {
    return fn.apply(new Builder(location)).build();
  }

  public Multi2VecGoogleVectorizer(
      String projectId,
      String model,
      Integer dimensions,
      String location,
      Integer videoIntervalSeconds,
      List<String> imageFields,
      List<String> videoFields,
      List<String> textFields,
      Weights weights,
      boolean vectorizeCollectionName,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.vectorizeCollectionName = false;

    this.projectId = projectId;
    this.model = model;
    this.dimensions = dimensions;
    this.location = location;
    this.videoIntervalSeconds = videoIntervalSeconds;
    this.imageFields = imageFields;
    this.videoFields = videoFields;
    this.textFields = textFields;
    this.weights = weights;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Multi2VecGoogleVectorizer(Builder builder) {
    this(
        builder.projectId,
        builder.model,
        builder.dimensions,
        builder.location,
        builder.videoIntervalSeconds,
        builder.imageFields.keySet().stream().toList(),
        builder.videoFields.keySet().stream().toList(),
        builder.textFields.keySet().stream().toList(),
        new Weights(
            builder.imageFields.values().stream().toList(),
            builder.videoFields.values().stream().toList(),
            builder.textFields.values().stream().toList()),
        builder.vectorizeCollectionName,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Multi2VecGoogleVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;

    private Map<String, Float> imageFields = new LinkedHashMap<>();
    private Map<String, Float> videoFields = new LinkedHashMap<>();
    private Map<String, Float> textFields = new LinkedHashMap<>();

    private final String projectId;
    private String model;
    private String location;
    private Integer dimensions;
    private Integer videoIntervalSeconds;

    public Builder(String projectId) {
      this.projectId = projectId;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    public Builder videoIntervalSeconds(int videoIntervalSeconds) {
      this.videoIntervalSeconds = videoIntervalSeconds;
      return this;
    }

    /** Add BLOB image properties to include in the embedding. */
    public Builder imageFields(List<String> fields) {
      fields.forEach(field -> imageFields.put(field, null));
      return this;
    }

    /** Add BLOB image properties to include in the embedding. */
    public Builder imageFields(String... fields) {
      return imageFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB image property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder imageField(String field, float weight) {
      imageFields.put(field, weight);
      return this;
    }

    /** Add BLOB video properties to include in the embedding. */
    public Builder videoFields(List<String> fields) {
      fields.forEach(field -> videoFields.put(field, null));
      return this;
    }

    /** Add BLOB video properties to include in the embedding. */
    public Builder videoFields(String... fields) {
      return videoFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB video property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder videoField(String field, float weight) {
      videoFields.put(field, weight);
      return this;
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(List<String> fields) {
      fields.forEach(field -> textFields.put(field, null));
      return this;
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(String... fields) {
      return textFields(Arrays.asList(fields));
    }

    /**
     * Add TEXT property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder textField(String field, float weight) {
      textFields.put(field, weight);
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

    @Override
    public Multi2VecGoogleVectorizer build() {
      return new Multi2VecGoogleVectorizer(this);
    }
  }
}
