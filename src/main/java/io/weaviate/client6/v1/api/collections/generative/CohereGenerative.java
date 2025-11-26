package io.weaviate.client6.v1.api.collections.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.generate.GenerativeProvider;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;

public record CohereGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("k") Integer topK,
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature,
    @SerializedName("returnLikelihoods") String returnLikelihoodsProperty,
    @SerializedName("stopSequences") List<String> stopSequences) implements Generative {

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

    /** Top K value for sampling. */
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

    /**
     * Set tokens which should signal the model to stop generating further output.
     */
    public Builder stopSequences(String... stopSequences) {
      return stopSequences(Arrays.asList(stopSequences));
    }

    /**
     * Set tokens which should signal the model to stop generating further output.
     */
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

    public static record ApiVersion(String version, Boolean deprecated, Boolean experimental) {
    }

    public static record BilledUnits(Double inputTokens, Double outputTokens, Double searchUnits,
        Double classifications) {
    }

    public static record Tokens(Double inputTokens, Double outputTokens) {
    }
  }

  public static record Provider(
      String baseUrl,
      Integer maxTokens,
      String model,
      Float temperature,
      Integer topK,
      Float topP,
      Float frequencyPenalty,
      Float presencePenalty,
      List<String> stopSequences) implements GenerativeProvider {

    public static Provider of(
        Function<CohereGenerative.Provider.Builder, ObjectBuilder<CohereGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeCohere.newBuilder();
      if (baseUrl != null) {
        provider.setBaseUrl(baseUrl);
      }
      if (maxTokens != null) {
        provider.setMaxTokens(maxTokens);
      }
      if (model != null) {
        provider.setModel(model);
      }
      if (temperature != null) {
        provider.setTemperature(temperature);
      }
      if (topK != null) {
        provider.setK(topK);
      }
      if (topP != null) {
        provider.setP(topP);
      }

      if (frequencyPenalty != null) {
        provider.setFrequencyPenalty(frequencyPenalty);
      }
      if (presencePenalty != null) {
        provider.setPresencePenalty(presencePenalty);
      }

      if (stopSequences != null) {
        provider.setStopSequences(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(stopSequences));
      }
      req.setCohere(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.baseUrl,
          builder.maxTokens,
          builder.model,
          builder.temperature,
          builder.topK,
          builder.topP,
          builder.frequencyPenalty,
          builder.presencePenalty,
          builder.stopSequences);
    }

    public static class Builder implements ObjectBuilder<CohereGenerative.Provider> {
      private String baseUrl;
      private Integer topK;
      private Float topP;
      private String model;
      private Integer maxTokens;
      private Float temperature;
      private Float frequencyPenalty;
      private Float presencePenalty;
      private final List<String> stopSequences = new ArrayList<>();

      /** Base URL of the generative provider. */
      public Builder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

      public Builder frequencyPenalty(float frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
      }

      /** Top P value for nucleus sampling. */
      public Builder presencePenalty(float presencePenalty) {
        this.presencePenalty = presencePenalty;
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
       * Set tokens which should signal the model to stop generating further output.
       */
      public Builder stopSequences(String... stopSequences) {
        return stopSequences(Arrays.asList(stopSequences));
      }

      /**
       * Set tokens which should signal the model to stop generating further output.
       */
      public Builder stopSequences(List<String> stopSequences) {
        this.stopSequences.addAll(stopSequences);
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
      public CohereGenerative.Provider build() {
        return new CohereGenerative.Provider(this);
      }
    }
  }
}
