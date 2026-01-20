package io.weaviate.client6.v1.api.collections.quantizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record BQ(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("rescore_limit") Integer rescoreLimit,
    @SerializedName("cache") Boolean cache) implements Quantization {

  @Override
  public Quantization.Kind _kind() {
    return Quantization.Kind.BQ;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static BQ of() {
    return of(ObjectBuilder.identity());
  }

  public static BQ of(Function<Builder, ObjectBuilder<BQ>> fn) {
    return fn.apply(new Builder()).build();
  }

  public BQ(Builder builder) {
    this(builder.enabled, builder.rescoreLimit, builder.cache);
  }

  public static class Builder implements ObjectBuilder<BQ> {
    private boolean enabled = true;
    private Integer rescoreLimit;
    private Boolean cache;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder rescoreLimit(int rescoreLimit) {
      this.rescoreLimit = rescoreLimit;
      return this;
    }

    public Builder cache(boolean enabled) {
      this.cache = enabled;
      return this;
    }

    @Override
    public BQ build() {
      return new BQ(this);
    }
  }
}
