package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record OllamaGenerative(
    @SerializedName("apiEndpoint") String apiEndpoint,
    @SerializedName("model") String model) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.OLLAMA;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static OllamaGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static OllamaGenerative of(Function<Builder, ObjectBuilder<OllamaGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public OllamaGenerative(Builder builder) {
    this(
        builder.apiEndpoint,
        builder.model);
  }

  public static class Builder implements ObjectBuilder<OllamaGenerative> {
    private String apiEndpoint;
    private String model;

    /** Destination endpoint of the generative provider. */
    public Builder apiEndpoint(String apiEndpoint) {
      this.apiEndpoint = apiEndpoint;
      return this;
    }

    /** Select generative model. */
    public Builder model(String model) {
      this.model = model;
      return this;
    }

    @Override
    public OllamaGenerative build() {
      return new OllamaGenerative(this);
    }
  }

  public static record Metadata() implements ProviderMetadata {

    @Override
    public Generative.Kind _kind() {
      return Generative.Kind.OLLAMA;
    }
  }
}
