package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

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
        builder.baseURL,
        builder.maxTokens,
        builder.topK,
        builder.topP,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<DatabricksGenerative> {
    private final String baseURL;

    private Integer maxTokens;
    private Integer topK;
    private Float topP;
    private Float temperature;

    public Builder(String baseURL) {
      this.baseURL = baseURL;
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

    @Override
    public Generative.Kind _kind() {
      return Generative.Kind.DATABRICKS;
    }
  }
}
