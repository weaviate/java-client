package io.weaviate.client6.v1.api.collections.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record CohereGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("kProperty") Integer topK,
    @SerializedName("model") String model,
    @SerializedName("maxTokensProperty") Integer maxTokens,
    @SerializedName("temperatureProperty") Float temperature,
    @SerializedName("returnLikelihoodsProperty") String returnLikelihoodsProperty,
    @SerializedName("stopSequencesProperty") List<String> stopSequences) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.COHERE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static CohereGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static CohereGenerative of(Function<Builder, ObjectBuilder<CohereGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public CohereGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.topK,
        builder.model,
        builder.maxTokens,
        builder.temperature,
        builder.returnLikelihoodsProperty,
        builder.stopSequences);
  }

  public static class Builder implements ObjectBuilder<CohereGenerative> {
    private String baseUrl;
    private Integer topK;
    private String model;
    private Integer maxTokens;
    private Float temperature;
    private String returnLikelihoodsProperty;
    private List<String> stopSequences = new ArrayList<>();

    /** Base URL of the generative provider. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder topK(int topK) {
      this.topK = topK;
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

    public Builder returnLikelihoodsProperty(String returnLikelihoodsProperty) {
      this.returnLikelihoodsProperty = returnLikelihoodsProperty;
      return this;
    }

    public Builder stopSequences(String... stopSequences) {
      return stopSequences(Arrays.asList(stopSequences));
    }

    public Builder stopSequences(List<String> stopSequences) {
      this.stopSequences = stopSequences;
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
    public CohereGenerative build() {
      return new CohereGenerative(this);
    }
  }

  public static record Metadata(ApiVersion apiVersion, BilledUnits billedUnits, Tokens tokens, List<String> warnings)
      implements ProviderMetadata {

    @Override
    public Generative.Kind _kind() {
      return Generative.Kind.COHERE;
    }

    public static record ApiVersion(String version, Boolean deprecated, Boolean experimental) {
    }

    public static record BilledUnits(Double inputTokens, Double outputTokens, Double searchUnits,
        Double classifications) {
    }

    public static record Tokens(Double inputTokens, Double outputTokens) {
    }
  }
}
