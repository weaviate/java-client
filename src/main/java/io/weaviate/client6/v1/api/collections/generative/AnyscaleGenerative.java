package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record AnyscaleGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.ANYSCALE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static AnyscaleGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static AnyscaleGenerative of(Function<Builder, ObjectBuilder<AnyscaleGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public AnyscaleGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<AnyscaleGenerative> {
    private String baseUrl;
    private String model;
    private Float temperature;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder temperature(float temperature) {
      this.temperature = temperature;
      return this;
    }

    @Override
    public AnyscaleGenerative build() {
      return new AnyscaleGenerative(this);
    }
  }
}
