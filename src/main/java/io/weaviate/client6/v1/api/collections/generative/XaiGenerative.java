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

public record XaiGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.XAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static XaiGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static XaiGenerative of(Function<Builder, ObjectBuilder<XaiGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public XaiGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.maxTokens,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<XaiGenerative> {
    private String baseUrl;
    private String model;
    private Integer maxTokens;
    private Float temperature;

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

    /** Select generative model. */
    public Builder model(String model) {
      this.model = model;
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
    public XaiGenerative build() {
      return new XaiGenerative(this);
    }
  }

  public static record Metadata(ProviderMetadata.Usage usage) implements ProviderMetadata {
  }

  public static record Provider(
      String baseUrl,
      Integer maxTokens,
      String model,
      Float temperature,
      Float topP,
      List<String> images,
      List<String> imageProperties) implements DynamicProvider {

    public static Provider of(
        Function<XaiGenerative.Provider.Builder, ObjectBuilder<XaiGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeXAI.newBuilder();
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
      if (topP != null) {
        provider.setTopP(topP);
      }
      if (images != null) {
        provider.setImages(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(images));
      }
      if (imageProperties != null) {
        provider.setImageProperties(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(imageProperties));
      }
      req.setXai(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.baseUrl,
          builder.maxTokens,
          builder.model,
          builder.temperature,
          builder.topP,
          builder.images,
          builder.imageProperties);
    }

    public static class Builder implements ObjectBuilder<XaiGenerative.Provider> {
      private String baseUrl;
      private Float topP;
      private String model;
      private Integer maxTokens;
      private Float temperature;
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

      /** Base URL of the generative provider. */
      public Builder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
      public XaiGenerative.Provider build() {
        return new XaiGenerative.Provider(this);
      }
    }
  }
}
