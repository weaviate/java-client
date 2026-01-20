package io.weaviate.client6.v1.api.collections.quantizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record RQ(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("rescore_limit") Integer rescoreLimit,
    @SerializedName("bits") Integer bits,
    @SerializedName("cache") Boolean cache) implements Quantization {

  @Override
  public Quantization.Kind _kind() {
    return Quantization.Kind.RQ;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static RQ of() {
    return of(ObjectBuilder.identity());
  }

  public static RQ of(Function<Builder, ObjectBuilder<RQ>> fn) {
    return fn.apply(new Builder()).build();
  }

  public RQ(Builder builder) {
    this(builder.enabled, builder.rescoreLimit, builder.bits, builder.cache);
  }

  public static class Builder implements ObjectBuilder<RQ> {
    private boolean enabled = true;
    private Integer rescoreLimit;
    private Integer bits;
    private Boolean cache;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder rescoreLimit(int rescoreLimit) {
      this.rescoreLimit = rescoreLimit;
      return this;
    }

    public Builder bits(int bits) {
      this.bits = bits;
      return this;
    }

    public Builder cache(boolean enabled) {
      this.cache = enabled;
      return this;
    }

    @Override
    public RQ build() {
      return new RQ(this);
    }
  }
}
