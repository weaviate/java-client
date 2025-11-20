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

    /** Base URL of the generative model. */
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
  }

  public static record Provider(
      String apiEndpoint,
      String model,
      Float temperature,
      List<String> images,
      List<String> imageProperties) implements GenerativeProvider {

    public static Provider of(
        Function<OllamaGenerative.Provider.Builder, ObjectBuilder<OllamaGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeOllama.newBuilder();
      if (apiEndpoint != null) {
        provider.setApiEndpoint(apiEndpoint);
      }
      if (model != null) {
        provider.setModel(model);
      }
      if (temperature != null) {
        provider.setTemperature(temperature);
      }
      if (images != null) {
        provider.setImages(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(images));
      }
      if (imageProperties != null) {
        provider.setImageProperties(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(imageProperties));
      }
      req.setOllama(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.apiEndpoint,
          builder.model,
          builder.temperature,
          builder.images,
          builder.imageProperties);
    }

    public static class Builder implements ObjectBuilder<OllamaGenerative.Provider> {
      private String apiEndpoint;
      private String model;
      private Float temperature;
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

      /** Base URL of the generative provider. */
      public Builder apiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
        return this;
      }

      /** Select generative model. */
      public Builder model(String model) {
        this.model = model;
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
      public OllamaGenerative.Provider build() {
        return new OllamaGenerative.Provider(this);
      }
    }
  }
}
