package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record NvidiaGenerative(
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.NVIDIA;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static NvidiaGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static NvidiaGenerative of(Function<Builder, ObjectBuilder<NvidiaGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public NvidiaGenerative(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.maxTokens,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<NvidiaGenerative> {
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
    public NvidiaGenerative build() {
      return new NvidiaGenerative(this);
    }
  }

  public static record Metadata(ProviderMetadata.Usage usage) implements ProviderMetadata {

    @Override
    public Generative.Kind _kind() {
      return Generative.Kind.NVIDIA;
    }
  }
}
