package io.weaviate.client6.v1.api.collections.quantizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record PQ(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("centroids") Integer centroids,
    @SerializedName("segments") Integer segments,
    @SerializedName("encoder_type") EncoderType encoderType,
    @SerializedName("encoder_distribusion") EncoderDistribution encoderDistribution,
    @SerializedName("training_limit") Integer trainingLimit,
    @SerializedName("bit_compression") Boolean bitCompression) implements Quantization {

  public enum EncoderType {
    @SerializedName("kmeans")
    KMEANS,
    @SerializedName("tile")
    TILE;
  }

  public enum EncoderDistribution {
    @SerializedName("log-normal")
    NORMAL,
    @SerializedName("normal")
    LOG_NORMAL;
  }

  @Override
  public Quantization.Kind _kind() {
    return Quantization.Kind.RQ;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static PQ of() {
    return of(ObjectBuilder.identity());
  }

  public static PQ of(Function<Builder, ObjectBuilder<PQ>> fn) {
    return fn.apply(new Builder()).build();
  }

  public PQ(Builder builder) {
    this(
        builder.enabled,
        builder.centroids,
        builder.segments,
        builder.encoderType,
        builder.encoderDistribution,
        builder.trainingLimit,
        builder.bitCompression);
  }

  public static class Builder implements ObjectBuilder<PQ> {
    private boolean enabled = true;
    private Integer centroids;
    private Integer segments;
    private EncoderType encoderType;
    private EncoderDistribution encoderDistribution;
    private Integer trainingLimit;
    private Boolean bitCompression;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder centroids(int centroids) {
      this.centroids = centroids;
      return this;
    }

    public Builder segments(int segments) {
      this.segments = segments;
      return this;
    }

    public Builder encoderType(EncoderType encoderType) {
      this.encoderType = encoderType;
      return this;
    }

    public Builder encoderDistribution(EncoderDistribution encoderDistribution) {
      this.encoderDistribution = encoderDistribution;
      return this;
    }

    public Builder trainingLimit(int trainingLimit) {
      this.trainingLimit = trainingLimit;
      return this;
    }

    public Builder bitCompression(boolean enabled) {
      this.bitCompression = enabled;
      return this;
    }

    @Override
    public PQ build() {
      return new PQ(this);
    }
  }
}
