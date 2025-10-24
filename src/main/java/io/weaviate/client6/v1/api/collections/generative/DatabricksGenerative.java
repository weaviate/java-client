package io.weaviate.client6.v1.api.collections.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.generate.DynamicProvider;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;

public record DatabricksGenerative(
    @SerializedName("endpoint") String baseUrl,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("topK") Integer topK,
    @SerializedName("topP") Float topP,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.DATABRICKS;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static DatabricksGenerative of(String baseURL) {
    return of(baseURL, ObjectBuilder.identity());
  }

  public static DatabricksGenerative of(String baseURL, Function<Builder, ObjectBuilder<DatabricksGenerative>> fn) {
    return fn.apply(new Builder(baseURL)).build();
  }

  public DatabricksGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.maxTokens,
        builder.topK,
        builder.topP,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<DatabricksGenerative> {
    private final String baseUrl;

    private Integer maxTokens;
    private Integer topK;
    private Float topP;
    private Float temperature;

    public Builder(String baseUrl) {
      this.baseUrl = baseUrl;
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
    public DatabricksGenerative build() {
      return new DatabricksGenerative(this);
    }
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
      Boolean logProbs,
      Integer topLogProbs,
      List<String> stopSequences) implements DynamicProvider {

    public static Provider of(
        Function<DatabricksGenerative.Provider.Builder, ObjectBuilder<DatabricksGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeDatabricks.newBuilder();
      if (baseUrl != null) {
        provider.setEndpoint(baseUrl);
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
      if (logProbs != null) {
        provider.setLogProbs(logProbs);
      }
      if (topLogProbs != null) {
        provider.setTopLogProbs(topLogProbs);
      }
      if (stopSequences != null) {
        provider.setStop(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(stopSequences));
      }
      req.setDatabricks(provider);
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
          builder.logProbs,
          builder.topLogProbs,
          builder.stopSequences);
    }

    public static class Builder implements ObjectBuilder<DatabricksGenerative.Provider> {
      private String baseUrl;
      private Integer n;
      private Float topP;
      private String model;
      private Integer maxTokens;
      private Float temperature;
      private Float frequencyPenalty;
      private Float presencePenalty;
      private Boolean logProbs;
      private Integer topLogProbs;
      private final List<String> stopSequences = new ArrayList<>();

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

      public Builder logProbs(boolean logProbs) {
        this.logProbs = logProbs;
        return this;
      }

      public Builder topLogProbs(int topLogProbs) {
        this.topLogProbs = topLogProbs;
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
      public DatabricksGenerative.Provider build() {
        return new DatabricksGenerative.Provider(this);
      }
    }
  }
}
