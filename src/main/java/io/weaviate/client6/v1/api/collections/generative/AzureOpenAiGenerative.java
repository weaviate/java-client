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

public record AzureOpenAiGenerative(
    @SerializedName("apiVersion") String apiVersion,
    @SerializedName("baseURL") String baseUrl,
    @SerializedName("frequencyPenalty") Float frequencyPenalty,
    @SerializedName("presencePenalty") Float presencePenalty,
    @SerializedName("maxTokens") Integer maxTokens,
    @SerializedName("temperature") Float temperature,
    @SerializedName("topP") Float topP,
    @SerializedName("model") String model,

    @SerializedName("resourceName") String resourceName,
    @SerializedName("deploymentId") String deploymentId) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.AZURE_OPENAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static AzureOpenAiGenerative of(String resourceName, String deploymentId) {
    return of(resourceName, deploymentId, ObjectBuilder.identity());
  }

  public static AzureOpenAiGenerative of(String resourceName, String deploymentId,
      Function<Builder, ObjectBuilder<AzureOpenAiGenerative>> fn) {
    return fn.apply(new Builder(resourceName, deploymentId)).build();
  }

  public AzureOpenAiGenerative(Builder builder) {
    this(
        builder.apiVersion,
        builder.baseUrl,
        builder.frequencyPenalty,
        builder.presencePenalty,
        builder.maxTokens,
        builder.temperature,
        builder.topP,
        builder.model,
        builder.resourceName,
        builder.deploymentId);
  }

  public static class Builder implements ObjectBuilder<AzureOpenAiGenerative> {
    private final String resourceName;
    private final String deploymentId;

    private String apiVersion;
    private String baseUrl;
    private Float frequencyPenalty;
    private Float presencePenalty;
    private Integer maxTokens;
    private Float temperature;
    private Float topP;
    private String model;

    public Builder(String resourceName, String deploymentId) {
      this.resourceName = resourceName;
      this.deploymentId = deploymentId;
    }

    /** API version for the generative provider. */
    public Builder apiVersion(String apiVersion) {
      this.apiVersion = apiVersion;
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

    public Builder frequencyPenalty(float frequencyPenalty) {
      this.frequencyPenalty = frequencyPenalty;
      return this;
    }

    public Builder presencePenalty(float presencePenalty) {
      this.presencePenalty = presencePenalty;
      return this;
    }

    /** Top P value for nucleus sampling. */
    public Builder topP(float topP) {
      this.topP = topP;
      return this;
    }

    @Override
    public AzureOpenAiGenerative build() {
      return new AzureOpenAiGenerative(this);
    }
  }

  public static record Provider(
      String baseUrl,
      Integer maxTokens,
      String model,
      Float temperature,
      Integer n,
      Float topP,
      Float frequencyPenalty,
      Float presencePenalty,
      String apiVersion,
      String resourceName,
      String deploymentId,
      List<String> stopSequences,
      List<String> images,
      List<String> imageProperties) implements GenerativeProvider {

    public static Provider of(
        Function<AzureOpenAiGenerative.Provider.Builder, ObjectBuilder<AzureOpenAiGenerative.Provider>> fn) {
      return fn.apply(new Builder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeOpenAI.newBuilder();
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
      if (frequencyPenalty != null) {
        provider.setFrequencyPenalty(frequencyPenalty);
      }
      if (presencePenalty != null) {
        provider.setPresencePenalty(presencePenalty);
      }
      if (apiVersion != null) {
        provider.setApiVersion(apiVersion);
      }
      if (resourceName != null) {
        provider.setResourceName(resourceName);
      }
      if (deploymentId != null) {
        provider.setDeploymentId(deploymentId);
      }
      if (stopSequences != null) {
        provider.setStop(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(stopSequences));
      }
      provider.setIsAzure(true);
      req.setOpenai(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.baseUrl,
          builder.maxTokens,
          builder.model,
          builder.temperature,
          builder.n,
          builder.topP,
          builder.frequencyPenalty,
          builder.presencePenalty,
          builder.apiVersion,
          builder.resourceName,
          builder.deploymentId,
          builder.stopSequences,
          builder.images,
          builder.imageProperties);
    }

    public static class Builder implements ObjectBuilder<AzureOpenAiGenerative.Provider> {
      private String baseUrl;
      private Integer n;
      private Float topP;
      private String model;
      private Integer maxTokens;
      private Float temperature;
      private Float frequencyPenalty;
      private Float presencePenalty;
      private String apiVersion;
      private String resourceName;
      private String deploymentId;
      private final List<String> stopSequences = new ArrayList<>();
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

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

      public Builder frequencyPenalty(float frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
      }

      /** Top P value for nucleus sampling. */
      public Builder presencePenalty(float presencePenalty) {
        this.presencePenalty = presencePenalty;
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
       * Set tokens which should signal the model to stop generating further output.
       */
      public Builder stopSequences(String... stopSequences) {
        return stopSequences(Arrays.asList(stopSequences));
      }

      /**
       * Set tokens which should signal the model to stop generating further output.
       */
      public Builder stopSequences(List<String> stopSequences) {
        this.stopSequences.addAll(stopSequences);
        return this;
      }

      public Builder apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
      }

      public Builder resourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
      }

      public Builder deploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
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
      public AzureOpenAiGenerative.Provider build() {
        return new AzureOpenAiGenerative.Provider(this);
      }
    }
  }
}
