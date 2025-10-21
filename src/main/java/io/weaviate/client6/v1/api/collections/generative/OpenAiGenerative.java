package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record OpenAiGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("frequencyPenaltyProperty") Float frequencyPenalty,
    @SerializedName("presencePenaltyProperty") Float presencePenalty,
    @SerializedName("maxTokensProperty") Integer maxTokens,
    @SerializedName("temperatureProperty") Float temperature,
    @SerializedName("topPProperty") Float topP,

    @SerializedName("model") String model) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.OPENAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static OpenAiGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static OpenAiGenerative of(Function<Builder, ObjectBuilder<OpenAiGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public OpenAiGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.frequencyPenalty,
        builder.presencePenalty,
        builder.maxTokens,
        builder.temperature,
        builder.topP,
        builder.model);
  }

  public static class Builder implements ObjectBuilder<OpenAiGenerative> {
    private String baseUrl;
    private Float frequencyPenalty;
    private Float presencePenalty;
    private Integer maxTokens;
    private Float temperature;
    private Float topP;
    private String model;

    /** Base URL of the generative provider. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Select generative model. */
    public Builder model(String model) {
      this.model = model;
      return this;
    }

    /** Limit the number of tokens to generate in the response. */
    public Builder maxTokens(int maxTokens) {
      this.maxTokens = maxTokens;
      return this;
    }

    /**
     * Control the randomness of the model's output.
     * Higher values make output more random.
     */
    public Builder temperature(float temperature) {
      this.temperature = temperature;
      return this;
    }

    public Builder frequencyPenalty(float frequencyPenalty) {
      this.frequencyPenalty = frequencyPenalty;
      return this;
    }

    public Builder presencePenalty(float presencePenalty) {
      this.presencePenalty = presencePenalty;
      return this;
    }

    /** Top P value for nucleus sampling. */
    public Builder topP(float topP) {
      this.topP = topP;
      return this;
    }

    @Override
    public OpenAiGenerative build() {
      return new OpenAiGenerative(this);
    }
  }
}
