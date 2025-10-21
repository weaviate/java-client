package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record MistralGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.MISTRAL;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static MistralGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static MistralGenerative of(Function<Builder, ObjectBuilder<MistralGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public MistralGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.maxTokens,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<MistralGenerative> {
    private String baseUrl;
    private String model;
    private Integer maxTokens;
    private Float temperature;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder maxTokens(int maxTokens) {
      this.maxTokens = maxTokens;
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
    public MistralGenerative build() {
      return new MistralGenerative(this);
    }
  }
}
