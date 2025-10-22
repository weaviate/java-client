package io.weaviate.client6.v1.api.collections.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TaggedUnion;

public record AnthropicGenerative(
    @SerializedName("model") String model,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature,
    @SerializedName("topK") Integer topK,
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
        builder.stopSequences);
  }

  public static class Builder implements ObjectBuilder<AnthropicGenerative> {
    private Integer topK;
    private String model;
    private Integer maxTokens;
    private Float temperature;
    private List<String> stopSequences = new ArrayList<>();

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
    public AnthropicGenerative build() {
      return new AnthropicGenerative(this);
    }
  }

  public static record Metadata(Usage usage) implements ProviderMetadata {

    @Override
    public Generative.Kind _kind() {
      return Generative.Kind.ANTHROPIC;
    }

    public static record Usage(Long inputTokens, Long outputTokens) {
    }
  }
}
