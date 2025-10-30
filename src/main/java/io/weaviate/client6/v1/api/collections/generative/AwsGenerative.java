package io.weaviate.client6.v1.api.collections.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.generate.DynamicProvider;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecAwsVectorizer.Service;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;

public record AwsGenerative(
    @SerializedName("region") String region,
    @SerializedName("service") Service service,
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

  public static AwsGenerative bedrock(String region, String model) {
    return bedrock(region, model, ObjectBuilder.identity());
  }

  public static AwsGenerative bedrock(String region, String model,
      Function<BedrockBuilder, ObjectBuilder<AwsGenerative>> fn) {
    return fn.apply(new BedrockBuilder(region, model)).build();
  }

  public static AwsGenerative sagemaker(String region, String baseUrl) {
    return sagemaker(region, baseUrl, ObjectBuilder.identity());
  }

  public static AwsGenerative sagemaker(String region, String baseUrl,
      Function<SagemakerBuilder, ObjectBuilder<AwsGenerative>> fn) {
    return fn.apply(new SagemakerBuilder(region, baseUrl)).build();
  }

  public AwsGenerative(Builder builder) {
    this(
        builder.region,
        builder.service,
        builder.baseUrl,
        builder.model);
  }

  public static class Builder implements ObjectBuilder<AwsGenerative> {
    private final String region;
    private final Service service;

    public Builder(Service service, String region) {
      this.service = service;
      this.region = region;
    }

    private String baseUrl;
    private String model;

    /** Base URL of the generative provider. */
    protected Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Select generative model. */
    protected Builder model(String model) {
      this.model = model;
      return this;
    }

    @Override
    public AwsGenerative build() {
      return new AwsGenerative(this);
    }
  }

  public static class BedrockBuilder extends Builder {
    public BedrockBuilder(String region, String model) {
      super(Service.BEDROCK, region);
      super.model(model);
    }

    @Override
    /** Required for {@link Service#BEDROCK}. */
    public Builder model(String model) {
      return super.model(model);
    }
  }

  public static class SagemakerBuilder extends Builder {
    public SagemakerBuilder(String region, String baseUrl) {
      super(Service.SAGEMAKER, region);
      super.baseUrl(baseUrl);
    }

    /** Required for {@link Service#SAGEMAKER}. */
    public Builder baseUrl(String baseUrl) {
      return super.baseUrl(baseUrl);
    }
  }

  public static record Metadata() implements ProviderMetadata {
  }

  public static record Provider(
      String region,
      Service service,
      String baseUrl,
      String model,
      String targetModel,
      String targetModelVariant,
      Float temperature,
      List<String> images,
      List<String> imageProperties) implements DynamicProvider {

    public static Provider bedrock(
        String region,
        String model,
        Function<AwsGenerative.Provider.BedrockBuilder, ObjectBuilder<AwsGenerative.Provider>> fn) {
      return fn.apply(new BedrockBuilder(region, model)).build();
    }

    public static Provider sagemaker(
        String region,
        String baseUrl,
        Function<AwsGenerative.Provider.SagemakerBuilder, ObjectBuilder<AwsGenerative.Provider>> fn) {
      return fn.apply(new SagemakerBuilder(region, baseUrl)).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeAWS.newBuilder();
      if (region != null) {
        provider.setRegion(region);
      }
      if (service != null) {
        provider.setService(
            service == Service.BEDROCK ? "bedrock"
                : service == Service.SAGEMAKER ? "sagemaker"
                    : "unknown");
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

    public abstract static class Builder implements ObjectBuilder<AwsGenerative.Provider> {
      private final Service service;
      private final String region;
      private String baseUrl;
      private String model;
      private String targetModel;
      private String targetModelVariant;
      private Float temperature;
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

      protected Builder(Service service, String region) {
        this.service = service;
        this.region = region;
      }

      /** Base URL of the generative provider. */
      protected Builder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
      }

      /** Select generative model. */
      protected Builder model(String model) {
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

    public static class BedrockBuilder extends Builder {
      public BedrockBuilder(String region, String model) {
        super(Service.BEDROCK, region);
        super.model(model);
      }

      @Override
      /** Required for {@link Service#BEDROCK}. */
      public Builder model(String model) {
        return super.model(model);
      }
    }

    public static class SagemakerBuilder extends Builder {
      public SagemakerBuilder(String region, String baseUrl) {
        super(Service.SAGEMAKER, region);
        super.baseUrl(baseUrl);
      }

      /** Required for {@link Service#SAGEMAKER}. */
      public Builder baseUrl(String baseUrl) {
        return super.baseUrl(baseUrl);
      }
    }
  }
}
