package io.weaviate.client6.v1.api.collections.quantizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record PQ(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("centroids") Integer centroids,
    @SerializedName("segments") Integer segments,
    /**
     * Encoder settings, which the server nests one level deeper as
     * {@code encoder: {type, distribution}}.
     */
    @SerializedName("encoder") Encoder encoder,
    @SerializedName("trainingLimit") Integer trainingLimit,
    @SerializedName("bitCompression") Boolean bitCompression) implements Quantization {

  /** Type of the encoder, or {@code null} if it was left at the server default. */
  public EncoderType encoderType() {
    return encoder != null ? encoder.type() : null;
  }

  /** Encoder distribution, or {@code null} if left at the server default. */
  public EncoderDistribution encoderDistribution() {
    return encoder != null ? encoder.distribution() : null;
  }

  public record Encoder(
      @SerializedName("type") EncoderType type,
      @SerializedName("distribution") EncoderDistribution distribution) {
  }

  public enum EncoderType {
    @SerializedName("kmeans")
    KMEANS,
    @SerializedName("tile")
    TILE;
  }

  public enum EncoderDistribution {
    @SerializedName("normal")
    NORMAL,
    @SerializedName("log-normal")
    LOG_NORMAL;
  }

  @Override
  public Quantization.Kind _kind() {
    return Quantization.Kind.PQ;
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
        builder.encoderType == null && builder.encoderDistribution == null
            ? null
            : new Encoder(builder.encoderType, builder.encoderDistribution),
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
