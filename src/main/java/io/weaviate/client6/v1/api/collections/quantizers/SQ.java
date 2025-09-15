package io.weaviate.client6.v1.api.collections.quantizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record SQ(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("rescore_limit") Integer rescoreLimit,
    @SerializedName("training_limit") Integer trainingLimit,
    @SerializedName("cache") Boolean cache) implements Quantization {

  @Override
  public Quantization.Kind _kind() {
    return Quantization.Kind.SQ;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static SQ of() {
    return of(ObjectBuilder.identity());
  }

  public static SQ of(Function<Builder, ObjectBuilder<SQ>> fn) {
    return fn.apply(new Builder()).build();
  }

  public SQ(Builder builder) {
    this(builder.enabled, builder.rescoreLimit, builder.trainingLimit, builder.cache);
  }

  public static class Builder implements ObjectBuilder<SQ> {
    private boolean enabled = true;
    private Integer rescoreLimit;
    private Integer trainingLimit;
    private Boolean cache;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder rescoreLimit(int rescoreLimit) {
      this.rescoreLimit = rescoreLimit;
      return this;
    }

    public Builder trainingLimit(int trainingLimit) {
      this.trainingLimit = trainingLimit;
      return this;
    }

    public Builder cache(boolean enabled) {
      this.cache = enabled;
      return this;
    }

    @Override
    public SQ build() {
      return new SQ(this);
    }
  }
}
