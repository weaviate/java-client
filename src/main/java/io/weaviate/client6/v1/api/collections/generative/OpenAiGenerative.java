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

public record OpenAiGenerative(
    @SerializedName("apiVersion") String apiVersion,
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("frequencyPenalty") Float frequencyPenalty,
    @SerializedName("presencePenalty") Float presencePenalty,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature,
    @SerializedName("topP") Float topP,
    @SerializedName("model") String model,
    @SerializedName("reasoningEffort") ReasoningEffort reasoningEffort,
    @SerializedName("verbosity") Verbosity verbosity) implements Generative {

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
        builder.apiVersion,
        builder.baseUrl,
        builder.frequencyPenalty,
        builder.presencePenalty,
        builder.maxTokens,
        builder.temperature,
        builder.topP,
        builder.model,
        builder.reasoningEffort,
        builder.verbosity);
  }

  public static class Builder implements ObjectBuilder<OpenAiGenerative> {
    private String apiVersion;
    private String baseUrl;
    private Float frequencyPenalty;
    private Float presencePenalty;
    private Integer maxTokens;
    private Float temperature;
    private Float topP;
    private String model;
    private ReasoningEffort reasoningEffort;
    private Verbosity verbosity;

    /** API version for the generative provider. */
    public Builder apiVersion(String apiVersion) {
      this.apiVersion = apiVersion;
      return this;
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

    /** Set the reasoning effort level. */
    public Builder reasoningEffort(ReasoningEffort reasoningEffort) {
      this.reasoningEffort = reasoningEffort;
      return this;
    }

    /** Set the verbosity level. */
    public Builder verbosity(Verbosity verbosity) {
      this.verbosity = verbosity;
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

  public enum ReasoningEffort {
    @SerializedName("minimal")
    MINIMAL,
    @SerializedName("low")
    LOW,
    @SerializedName("medium")
    MEDIUM,
    @SerializedName("high")
    HIGH;
  }

  public enum Verbosity {
    @SerializedName("low")
    LOW,
    @SerializedName("medium")
    MEDIUM,
    @SerializedName("high")
    HIGH;
  }

  public static record Metadata(ProviderMetadata.Usage usage) implements ProviderMetadata {
  }

  public static record Provider(
      String baseUrl,
      Integer maxTokens,
      String model,
      Float temperature,
      Integer n,
      Float topP,
      Float frequencyPenalty,
      Float presencePenalty,
      List<String> stopSequences,
      List<String> images,
      List<String> imageProperties) implements GenerativeProvider {

    public static Provider of(
        Function<OpenAiGenerative.Provider.Builder, ObjectBuilder<OpenAiGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeOpenAI.newBuilder();
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
      if (n != null) {
        provider.setN(n);
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
      provider.setIsAzure(false);
      req.setOpenai(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.baseUrl,
          builder.maxTokens,
          builder.model,
          builder.temperature,
          builder.n,
          builder.topP,
          builder.frequencyPenalty,
          builder.presencePenalty,
          builder.stopSequences,
          builder.images,
          builder.imageProperties);
    }

    public static class Builder implements ObjectBuilder<OpenAiGenerative.Provider> {
      private String baseUrl;
      private Integer n;
      private Float topP;
      private String model;
      private Integer maxTokens;
      private Float temperature;
      private Float frequencyPenalty;
      private Float presencePenalty;
      private final List<String> stopSequences = new ArrayList<>();
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

      /** Base URL of the generative provider. */
      public Builder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
      }

      public Builder n(int n) {
        this.n = n;
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

      public Builder images(String... images) {
        return images(Arrays.asList(images));
      }

      public Builder images(List<String> images) {
        this.images.addAll(images);
        return this;
      }

      public Builder imageProperties(String... imageProperties) {
        return imageProperties(Arrays.asList(imageProperties));
      }

      public Builder imageProperties(List<String> imageProperties) {
        this.imageProperties.addAll(imageProperties);
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
      public OpenAiGenerative.Provider build() {
        return new OpenAiGenerative.Provider(this);
      }
    }
  }
}
