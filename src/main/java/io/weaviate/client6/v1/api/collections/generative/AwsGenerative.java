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

public record AwsGenerative(
    @SerializedName("region") String region,
    @SerializedName("service") String service,
    @SerializedName("endpoint") String baseUrl,
    @SerializedName("model") String model) implements Generative {

  @Override
  public Generative.Kind _kind() {
    return Generative.Kind.AWS;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static AwsGenerative of(String region, String service) {
    return of(region, service, ObjectBuilder.identity());
  }

  public static AwsGenerative of(String region, String service, Function<Builder, ObjectBuilder<AwsGenerative>> fn) {
    return fn.apply(new Builder(region, service)).build();
  }

  public AwsGenerative(Builder builder) {
    this(
        builder.service,
        builder.region,
        builder.baseUrl,
        builder.model);
  }

  public static class Builder implements ObjectBuilder<AwsGenerative> {
    private final String region;
    private final String service;

    public Builder(String service, String region) {
      this.service = service;
      this.region = region;
    }

    private String baseUrl;
    private String model;

    /** Base URL of the generative provider. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Select generative model. */
    public Builder model(String model) {
      this.model = model;
      return this;
    }

    @Override
    public AwsGenerative build() {
      return new AwsGenerative(this);
    }
  }

  public static record Metadata() implements ProviderMetadata {

    @Override
    public Generative.Kind _kind() {
      return Generative.Kind.AWS;
    }
  }

  public static record Provider(
      String region,
      String service,
      String baseUrl,
      String model,
      String targetModel,
      String targetModelVariant,
      Float temperature,
      List<String> images,
      List<String> imageProperties) implements DynamicProvider {

    public static Provider of(
        Function<AwsGenerative.Provider.Builder, ObjectBuilder<AwsGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeAWS.newBuilder();
      if (region != null) {
        provider.setRegion(region);
      }
      if (service != null) {
        provider.setService(service);
      }
      if (baseUrl != null) {
        provider.setEndpoint(baseUrl);
      }
      if (model != null) {
        provider.setModel(model);
      }
      if (temperature != null) {
        provider.setTemperature(temperature);
      }
      if (targetModel != null) {
        provider.setTargetModel(targetModel);
      }
      if (targetModelVariant != null) {
        provider.setTargetVariant(targetModelVariant);
      }
      if (images != null) {
        provider.setImages(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(images));
      }
      if (imageProperties != null) {
        provider.setImageProperties(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(imageProperties));
      }
      req.setAws(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.region,
          builder.service,
          builder.baseUrl,
          builder.model,
          builder.targetModel,
          builder.targetModelVariant,
          builder.temperature,
          builder.images,
          builder.imageProperties);
    }

    public static class Builder implements ObjectBuilder<AwsGenerative.Provider> {
      private String region;
      private String service;
      private String baseUrl;
      private String model;
      private String targetModel;
      private String targetModelVariant;
      private Float temperature;
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

      public Builder region(String region) {
        this.region = region;
        return this;
      }

      public Builder service(String service) {
        this.service = service;
        return this;
      }

      /** Base URL of the generative provider. */
      public Builder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
      }

      /** Select generative model. */
      public Builder model(String model) {
        this.model = model;
        return this;
      }

      public Builder targetModel(String targetModel) {
        this.targetModel = targetModel;
        return this;
      }

      public Builder targetModelVariant(String targetModelVariant) {
        this.targetModelVariant = targetModelVariant;
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
      public AwsGenerative.Provider build() {
        return new AwsGenerative.Provider(this);
      }
    }
  }
}
