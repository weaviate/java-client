package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record AzureOpenAiGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("frequencyPenaltyProperty") Float frequencyPenalty,
    @SerializedName("presencePenaltyProperty") Float presencePenalty,
    @SerializedName("maxTokensProperty") Integer maxTokens,
    @SerializedName("temperatureProperty") Float temperature,
    @SerializedName("topPProperty") Float topP,

    @SerializedName("resourceName") String resourceName,
    @SerializedName("deploymentId") String deploymentId) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.AZURE_OPENAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static AzureOpenAiGenerative of(String resourceName, String deploymentId) {
    return of(resourceName, deploymentId, ObjectBuilder.identity());
  }

  public static AzureOpenAiGenerative of(String resourceName, String deploymentId,
      Function<Builder, ObjectBuilder<AzureOpenAiGenerative>> fn) {
    return fn.apply(new Builder(resourceName, deploymentId)).build();
  }

  public AzureOpenAiGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.frequencyPenalty,
        builder.presencePenalty,
        builder.maxTokens,
        builder.temperature,
        builder.topP,
        builder.resourceName,
        builder.deploymentId);
  }

  public static class Builder implements ObjectBuilder<AzureOpenAiGenerative> {
    private final String resourceName;
    private final String deploymentId;

    private String baseUrl;
    private Float frequencyPenalty;
    private Float presencePenalty;
    private Integer maxTokens;
    private Float temperature;
    private Float topP;

    public Builder(String resourceName, String deploymentId) {
      this.resourceName = resourceName;
      this.deploymentId = deploymentId;
    }

    /** Base URL of the generative provider. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
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
    public AzureOpenAiGenerative build() {
      return new AzureOpenAiGenerative(this);
    }
  }
}
