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

  public static DatabricksGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static DatabricksGenerative of(Function<Builder, ObjectBuilder<DatabricksGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public DatabricksGenerative(Builder builder) {
    this(
        builder.endpoint,
        builder.maxTokens,
        builder.topK,
        builder.topP,
        builder.temperature);
  }

  public static class Builder implements ObjectBuilder<DatabricksGenerative> {
    private String endpoint;
    private Integer maxTokens;
    private Integer topK;
    private Float topP;
    private Float temperature;

    public Builder endpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder maxTokens(int maxTokens) {
      this.maxTokens = maxTokens;
      return this;
    }

    public Builder topK(int topK) {
      this.topK = topK;
      return this;
    }

    public Builder topP(float topP) {
      this.topP = topP;
      return this;
    }

    public Builder temperature(float temperature) {
      this.temperature = temperature;
      return this;
    }

    @Override
    public DatabricksGenerative build() {
      return new DatabricksGenerative(this);
    }
  }
}
