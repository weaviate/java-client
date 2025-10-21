package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record GoogleGenerative(
    @SerializedName("apiEndpoint") String baseUrl,
    @SerializedName("modelId") String model,
    @SerializedName("projectId") String projectId,
    @SerializedName("maxOutputTokens") Integer maxTokens,
    @SerializedName("topK") Integer topK,
    @SerializedName("topP") Float topP,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.GOOGLE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static GoogleGenerative of(String projectId) {
    return of(projectId, ObjectBuilder.identity());
  }

  public static GoogleGenerative of(String projectId, Function<Builder, ObjectBuilder<GoogleGenerative>> fn) {
    return fn.apply(new Builder(projectId)).build();
  }

  public GoogleGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.projectId,
        builder.maxTokens,
        builder.topK,
        builder.topP,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<GoogleGenerative> {
    private final String projectId;

    private String baseUrl;
    private String model;
    private Integer maxTokens;
    private Integer topK;
    private Float topP;
    private Float temperature;

    public Builder(String projectId) {
      this.projectId = projectId;
    }

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

    /** Top K value for sampling. */
    public Builder topK(int topK) {
      this.topK = topK;
      return this;
    }

    /** Top P value for nucleus sampling. */
    public Builder topP(float topP) {
      this.topP = topP;
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

    @Override
    public GoogleGenerative build() {
      return new GoogleGenerative(this);
    }
  }
}
