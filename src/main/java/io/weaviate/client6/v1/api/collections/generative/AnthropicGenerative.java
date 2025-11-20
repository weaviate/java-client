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

public record AnthropicGenerative(
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature,
    @SerializedName("topK") Integer topK,
    @SerializedName("topP") Float topP,
    @SerializedName("stopSequences") List<String> stopSequences) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.ANTHROPIC;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static AnthropicGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static AnthropicGenerative of(Function<Builder, ObjectBuilder<AnthropicGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public AnthropicGenerative(Builder builder) {
    this(
        builder.model,
        builder.maxTokens,
        builder.temperature,
        builder.topK,
        builder.topP,
        builder.stopSequences);
  }

  public static class Builder implements ObjectBuilder<AnthropicGenerative> {
    private Integer topK;
    private Float topP;
    private String model;
    private Integer maxTokens;
    private Float temperature;
    private final List<String> stopSequences = new ArrayList<>();

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
    public AnthropicGenerative build() {
      return new AnthropicGenerative(this);
    }
  }

  public static record Metadata(Usage usage) implements ProviderMetadata {
    public static record Usage(Long inputTokens, Long outputTokens) {
    }
  }

  public static record Provider(
      String baseUrl,
      Integer maxTokens,
      String model,
      Float temperature,
      Integer topK,
      Float topP,
      List<String> stopSequences,
      List<String> images,
      List<String> imageProperties) implements GenerativeProvider {

    public static Provider of(
        Function<AnthropicGenerative.Provider.Builder, ObjectBuilder<AnthropicGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeAnthropic.newBuilder();
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
        provider.setTopK(topK);
      }
      if (topP != null) {
        provider.setTopP(topP);
      }

      if (stopSequences != null) {
        provider.setStopSequences(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(stopSequences));
      }
      if (images != null) {
        provider.setImages(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(images));
      }
      if (imageProperties != null) {
        provider.setImageProperties(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(imageProperties));
      }
      req.setAnthropic(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.baseUrl,
          builder.maxTokens,
          builder.model,
          builder.temperature,
          builder.topK,
          builder.topP,
          builder.stopSequences,
          builder.images,
          builder.imageProperties);
    }

    public static class Builder implements ObjectBuilder<AnthropicGenerative.Provider> {
      private String baseUrl;
      private Integer topK;
      private Float topP;
      private String model;
      private Integer maxTokens;
      private Float temperature;
      private final List<String> stopSequences = new ArrayList<>();
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

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
      public AnthropicGenerative.Provider build() {
        return new AnthropicGenerative.Provider(this);
      }
    }
  }
}
