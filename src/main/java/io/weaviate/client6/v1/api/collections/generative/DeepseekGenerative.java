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

public record DeepseekGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature,
    @SerializedName("frequencyPenalty") Float frequencyPenalty,
    @SerializedName("presencePenalty") Float presencePenalty,
    @SerializedName("topP") Float topP,
    @SerializedName("stop") List<String> stopSequences) implements Generative {

  @Override
  public Generative.Kind _kind() {
    return Generative.Kind.DEEPSEEK;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static DeepseekGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static DeepseekGenerative of(Function<Builder, ObjectBuilder<DeepseekGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public DeepseekGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.maxTokens,
        builder.temperature,
        builder.frequencyPenalty,
        builder.presencePenalty,
        builder.topP,
        builder.stopSequences);
  }

  public static class Builder implements ObjectBuilder<DeepseekGenerative> {
    private String baseUrl;
    private String model;
    private Float temperature;
    private Integer maxTokens;
    private Float frequencyPenalty;
    private Float presencePenalty;
    private Float topP;
    private List<String> stopSequences = new ArrayList<>();

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

    public Builder stopSequences(String... values) {
      return stopSequences(Arrays.asList(values));
    }

    public Builder stopSequences(List<String> values) {
      this.stopSequences.addAll(values);
      return this;
    }

    @Override
    public DeepseekGenerative build() {
      return new DeepseekGenerative(this);
    }
  }

  public static record Metadata(ProviderMetadata.Usage usage) implements ProviderMetadata {
  }

  public static record Provider(
      String baseUrl,
      String model,
      Integer maxTokens,
      Float temperature,
      Float frequencyPenalty,
      Float presencePenalty,
      Float topP,
      List<String> stopSequences) implements GenerativeProvider {

    public static Provider of(
        Function<DeepseekGenerative.Provider.Builder, ObjectBuilder<DeepseekGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeDeepseek.newBuilder();
      if (baseUrl != null) {
        provider.setBaseUrl(baseUrl);
      }
      if (model != null) {
        provider.setModel(model);
      }
      if (temperature != null) {
        provider.setTemperature(temperature);
      }
      if (maxTokens != null) {
        provider.setMaxTokens(maxTokens);
      }
      if (topP != null) {
        provider.setTopP(topP);
      }
      if (frequencyPenalty != null) {
        provider.setFrequencyPenalty(frequencyPenalty);
      }
      if (presencePenalty != null) {
        provider.setPresencePenalty(presencePenalty);
      }
      if (stopSequences != null) {
        provider.setStop(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(stopSequences));
      }
      req.setDeepseek(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.baseUrl,
          builder.model,
          builder.maxTokens,
          builder.temperature,
          builder.frequencyPenalty,
          builder.presencePenalty,
          builder.topP,
          builder.stopSequences);
    }

    public static class Builder implements ObjectBuilder<DeepseekGenerative.Provider> {
      private String baseUrl;
      private String model;
      private Float temperature;
      private Integer maxTokens;
      private Float frequencyPenalty;
      private Float presencePenalty;
      private Float topP;
      private List<String> stopSequences = new ArrayList<>();

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

      public Builder stop(String... values) {
        return stop(Arrays.asList(values));
      }

      public Builder stop(List<String> values) {
        this.stopSequences.addAll(values);
        return this;
      }

      @Override
      public DeepseekGenerative.Provider build() {
        return new DeepseekGenerative.Provider(this);
      }
    }
  }
}
