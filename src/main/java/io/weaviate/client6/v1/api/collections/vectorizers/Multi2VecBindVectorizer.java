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

public record Multi2VecBindVectorizer(
    /** BLOB image properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** BLOB audio properties included in the embedding. */
    @SerializedName("audioFields") List<String> audioFields,
    /** BLOB video properties included in the embedding. */
    @SerializedName("videoFields") List<String> videoFields,
    /** BLOB depth properties included in the embedding. */
    @SerializedName("depthFields") List<String> depthFields,
    /** BLOB thermal properties included in the embedding. */
    @SerializedName("thermalFields") List<String> thermalFields,
    /** BLOB IMU properties included in the embedding. */
    @SerializedName("imuFields") List<String> imuFields,
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
       * Weights of the BLOB audio properties. Values appear in the same order as the
       * corresponding property names in {@code audioFields}.
       */
      @SerializedName("audioWeights") List<Float> audioWeights,
      /**
       * Weights of the BLOB video properties. Values appear in the same order as the
       * corresponding property names in {@code videoFields}.
       */
      @SerializedName("videoWeights") List<Float> videoWeights,
      /**
       * Weights of the BLOB depth properties. Values appear in the same order as the
       * corresponding property names in {@code depthFields}.
       */
      @SerializedName("depthWeights") List<Float> depthWeights,
      /**
       * Weights of the BLOB thermal properties. Values appear in the same order as
       * the
       * corresponding property names in {@code thermalFields}.
       */
      @SerializedName("thermalWeights") List<Float> thermalWeights,
      /**
       * Weights of the BLOB IMU properties. Values appear in the same order as the
       * corresponding property names in {@code imuFields}.
       */
      @SerializedName("imuWeights") List<Float> imuWeights,
      /**
       * Weights of the TEXT properties. Values appear in the same order as the
       * corresponding property names in {@code textFields}.
       */
      @SerializedName("textWeights") List<Float> textWeights) {
  }

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.MULTI2VEC_BIND;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Multi2VecBindVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Multi2VecBindVectorizer of(Function<Builder, ObjectBuilder<Multi2VecBindVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Multi2VecBindVectorizer(
      List<String> imageFields,
      List<String> audioFields,
      List<String> videoFields,
      List<String> depthFields,
      List<String> thermalFields,
      List<String> imuFields,
      List<String> textFields,
      Weights weights,
      boolean vectorizeCollectionName,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.vectorizeCollectionName = false;

    this.imageFields = imageFields;
    this.audioFields = audioFields;
    this.videoFields = videoFields;
    this.depthFields = depthFields;
    this.thermalFields = thermalFields;
    this.imuFields = imuFields;
    this.textFields = textFields;
    this.weights = weights;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Multi2VecBindVectorizer(Builder builder) {
    this(
        builder.imageFields.keySet().stream().toList(),
        builder.audioFields.keySet().stream().toList(),
        builder.videoFields.keySet().stream().toList(),
        builder.depthFields.keySet().stream().toList(),
        builder.thermalFields.keySet().stream().toList(),
        builder.imuFields.keySet().stream().toList(),
        builder.textFields.keySet().stream().toList(),
        new Weights(
            builder.imageFields.values().stream().toList(),
            builder.audioFields.values().stream().toList(),
            builder.videoFields.values().stream().toList(),
            builder.depthFields.values().stream().toList(),
            builder.thermalFields.values().stream().toList(),
            builder.imuFields.values().stream().toList(),
            builder.textFields.values().stream().toList()),
        builder.vectorizeCollectionName,
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Multi2VecBindVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;

    private Map<String, Float> imageFields = new LinkedHashMap<>();
    private Map<String, Float> audioFields = new LinkedHashMap<>();
    private Map<String, Float> videoFields = new LinkedHashMap<>();
    private Map<String, Float> depthFields = new LinkedHashMap<>();
    private Map<String, Float> thermalFields = new LinkedHashMap<>();
    private Map<String, Float> imuFields = new LinkedHashMap<>();
    private Map<String, Float> textFields = new LinkedHashMap<>();

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

    /** Add BLOB audio properties to include in the embedding. */
    public Builder audioFields(List<String> fields) {
      fields.forEach(field -> audioFields.put(field, null));
      return this;
    }

    /** Add BLOB audio properties to include in the embedding. */
    public Builder audioFields(String... fields) {
      return audioFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB audio property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder audioField(String field, float weight) {
      audioFields.put(field, weight);
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

    /** Add BLOB depth properties to include in the embedding. */
    public Builder depthFields(List<String> fields) {
      fields.forEach(field -> depthFields.put(field, null));
      return this;
    }

    /** Add BLOB depth properties to include in the embedding. */
    public Builder depthFields(String... fields) {
      return depthFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB depth property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder depthField(String field, float weight) {
      depthFields.put(field, weight);
      return this;
    }

    /** Add BLOB thermal properties to include in the embedding. */
    public Builder thermalFields(List<String> fields) {
      fields.forEach(field -> thermalFields.put(field, null));
      return this;
    }

    /** Add BLOB thermal properties to include in the embedding. */
    public Builder thermalFields(String... fields) {
      return thermalFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB thermal property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder thermalField(String field, float weight) {
      thermalFields.put(field, weight);
      return this;
    }

    /** Add BLOB IMU properties to include in the embedding. */
    public Builder imuFields(List<String> fields) {
      fields.forEach(field -> imuFields.put(field, null));
      return this;
    }

    /** Add BLOB IMU properties to include in the embedding. */
    public Builder imuFields(String... fields) {
      return imuFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB IMU property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder imuField(String field, float weight) {
      imuFields.put(field, weight);
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
    public Multi2VecBindVectorizer build() {
      return new Multi2VecBindVectorizer(this);
    }
  }
}
