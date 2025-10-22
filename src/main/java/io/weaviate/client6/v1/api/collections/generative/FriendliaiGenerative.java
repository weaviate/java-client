package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.generate.DynamicProvider;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;

public record FriendliaiGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.FRIENDLIAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static FriendliaiGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static FriendliaiGenerative of(Function<Builder, ObjectBuilder<FriendliaiGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public FriendliaiGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.maxTokens,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<FriendliaiGenerative> {
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
    public FriendliaiGenerative build() {
      return new FriendliaiGenerative(this);
    }
  }

  public static record Metadata(ProviderMetadata.Usage usage) implements ProviderMetadata {

    @Override
    public Generative.Kind _kind() {
      return Generative.Kind.FRIENDLIAI;
    }
  }

  public static record Provider(
      String baseUrl,
      Integer maxTokens,
      String model,
      Float temperature,
      Integer n,
      Float topP) implements DynamicProvider {

    public static Provider of(
        Function<FriendliaiGenerative.Provider.Builder, ObjectBuilder<FriendliaiGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeFriendliAI.newBuilder();
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
      req.setFriendliai(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.baseUrl,
          builder.maxTokens,
          builder.model,
          builder.temperature,
          builder.n,
          builder.topP);
    }

    public static class Builder implements ObjectBuilder<FriendliaiGenerative.Provider> {
      private String baseUrl;
      private Integer n;
      private Float topP;
      private String model;
      private Integer maxTokens;
      private Float temperature;

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

      @Override
      public FriendliaiGenerative.Provider build() {
        return new FriendliaiGenerative.Provider(this);
      }
    }
  }
}
