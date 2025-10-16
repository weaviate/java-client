package io.weaviate.client6.v1.api.collections.encoding;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Encoding;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record MuveraEncoding(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("ksim") Integer ksim,
    @SerializedName("dprojections") Integer dprojections,
    @SerializedName("repetitions") Integer repetitions) implements Encoding {

  public static MuveraEncoding of() {
    return of(ObjectBuilder.identity());
  }

  public static MuveraEncoding of(Function<Builder, ObjectBuilder<MuveraEncoding>> fn) {
    return fn.apply(new Builder()).build();
  }

  public MuveraEncoding(Builder builder) {
    this(
        builder.enabled,
        builder.ksim,
        builder.dprojections,
        builder.repetitions);
  }

  public static class Builder implements ObjectBuilder<MuveraEncoding> {
    private boolean enabled = true;
    private Integer ksim;
    private Integer dprojections;
    private Integer repetitions;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder ksim(int ksim) {
      this.ksim = ksim;
      return this;
    }

    public Builder dprojections(int dprojections) {
      this.dprojections = dprojections;
      return this;
    }

    public Builder repetitions(int repetitions) {
      this.repetitions = repetitions;
      return this;
    }

    @Override
    public MuveraEncoding build() {
      return new MuveraEncoding(this);
    }
  }

  @Override
  public Encoding.Kind _kind() {
    return Encoding.Kind.MUVERA;
  }

  @Override
  public Object _self() {
    return this;
  }
}
