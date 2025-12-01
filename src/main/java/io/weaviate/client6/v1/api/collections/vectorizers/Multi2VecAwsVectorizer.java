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

public record Multi2VecAwsVectorizer(
    @SerializedName("model") String model,
    @SerializedName("dimensions") Integer dimensions,
    @SerializedName("region") String region,
    /** BLOB properties included in the embedding. */
    @SerializedName("imageFields") List<String> imageFields,
    /** TEXT properties included in the embedding. */
    @SerializedName("textFields") List<String> textFields,
    /** Weights of the included properties. */
    @SerializedName("weights") Weights weights,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  private static record Weights(
      /**
       * Weights of the BLOB properties. Values appear in the same order as the
       * corresponding property names in {@code imageFields}.
       */
      @SerializedName("imageWeights") List<Float> imageWeights,
      /**
       * Weights of the TEXT properties. Values appear in the same order as the
       * corresponding property names in {@code textFields}.
       */
      @SerializedName("textWeights") List<Float> textWeights) {
  }

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.MULTI2VEC_AWS;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Multi2VecAwsVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Multi2VecAwsVectorizer of(Function<Builder, ObjectBuilder<Multi2VecAwsVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Multi2VecAwsVectorizer(Builder builder) {
    this(
        builder.model,
        builder.dimensions,
        builder.region,
        builder.imageFields,
        builder.textFields,
        builder.getWeights(),
        builder.vectorIndex,
        builder.quantization);
  }

  public static class Builder implements ObjectBuilder<Multi2VecAwsVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private Quantization quantization;

    private List<String> imageFields;
    private List<Float> imageWeights;
    private List<String> textFields;
    private List<Float> textWeights;

    private String model;
    private Integer dimensions;
    private String region;

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    public Builder region(String region) {
      this.region = region;
      return this;
    }

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(List<String> fields) {
      this.imageFields = fields;
      return this;
    }

    /** Add BLOB properties to include in the embedding. */
    public Builder imageFields(String... fields) {
      return imageFields(Arrays.asList(fields));
    }

    /**
     * Add BLOB property to include in the embedding.
     *
     * @param field  Property name.
     * @param weight Custom weight between 0.0 and 1.0.
     */
    public Builder imageField(String field, float weight) {
      if (this.imageFields == null) {
        this.imageFields = new ArrayList<>();
      }
      if (this.imageWeights == null) {
        this.imageWeights = new ArrayList<>();
      }
      this.imageFields.add(field);
      this.imageWeights.add(weight);
      return this;
    }

    /** Add TEXT properties to include in the embedding. */
    public Builder textFields(List<String> fields) {
      this.textFields = fields;
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
      if (this.textFields == null) {
        this.textFields = new ArrayList<>();
      }
      if (this.textWeights == null) {
        this.textWeights = new ArrayList<>();
      }
      this.textFields.add(field);
      this.textWeights.add(weight);
      return this;
    }

    protected Weights getWeights() {
      if (this.textWeights != null || this.imageWeights != null) {
        return new Weights(this.imageWeights, this.textWeights);
      }
      return null;
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
    public Multi2VecAwsVectorizer build() {
      return new Multi2VecAwsVectorizer(this);
    }
  }
}
