package io.weaviate.client6.v1.api.collections.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.generate.GenerativeProvider;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecGoogleVectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;

public record GoogleGenerative(
    @SerializedName("apiEndpoint") String apiEndpoint,
    @SerializedName("modelId") String modelId,
    @SerializedName("projectId") String projectId,
    @SerializedName("maxOutputTokens") Integer maxTokens,
    @SerializedName("topK") Integer topK,
    @SerializedName("topP") Float topP,
    @SerializedName("temperature") Float temperature) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.GOOGLE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static GoogleGenerative aiStudio() {
    return aiStudio(ObjectBuilder.identity());
  }

  public static GoogleGenerative aiStudio(Function<AiStudioBuilder, ObjectBuilder<GoogleGenerative>> fn) {
    return fn.apply(new AiStudioBuilder()).build();
  }

  public static GoogleGenerative vertex(String projectId) {
    return vertex(projectId, ObjectBuilder.identity());
  }

  public static GoogleGenerative vertex(String projectId, Function<VertexBuilder, ObjectBuilder<GoogleGenerative>> fn) {
    return fn.apply(new VertexBuilder(projectId)).build();
  }

  public GoogleGenerative(Builder builder) {
    this(
        builder.apiEndpoint,
        builder.modelId,
        builder.projectId,
        builder.maxTokens,
        builder.topK,
        builder.topP,
        builder.temperature);
  }

  public abstract static class Builder implements ObjectBuilder<GoogleGenerative> {
    private String apiEndpoint;
    private final String projectId;

    private String modelId;
    private Integer maxTokens;
    private Integer topK;
    private Float topP;
    private Float temperature;

    public Builder(String apiEndpoint, String projectId) {
      this.projectId = projectId;
      this.apiEndpoint = apiEndpoint;
    }

    /** Base URL of the generative provider. */
    protected Builder apiEndpoint(String apiEndpoint) {
      this.apiEndpoint = apiEndpoint;
      return this;
    }

    /** Select generative model. */
    public Builder modelId(String modelId) {
      this.modelId = modelId;
      return this;
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
    public GoogleGenerative build() {
      return new GoogleGenerative(this);
    }
  }

  public static class AiStudioBuilder extends Builder {
    public AiStudioBuilder() {
      super(Text2VecGoogleVectorizer.AiStudioBuilder.BASE_URL, null);
    }
  }

  public static class VertexBuilder extends Builder {
    public VertexBuilder(String projectId) {
      super(Text2VecGoogleVectorizer.VertexBuilder.DEFAULT_BASE_URL, projectId);
    }

    /** Base URL of the generative provider. */
    public VertexBuilder apiEndpoint(String apiEndpoint) {
      super.apiEndpoint(apiEndpoint);
      return this;
    }
  }

  public static record Metadata(TokenMetadata tokens, Usage usage) implements ProviderMetadata {

    public static record TokenCount(Long totalBillableCharacters, Long totalTokens) {
    }

    public static record TokenMetadata(TokenCount inputTokens, TokenCount outputTokens) {
    }

    public static record Usage(Long promptTokenCount, Long candidatesTokenCount, Long totalTokenCount) {
    }
  }

  public static record Provider(
      String apiEndpoint,
      Integer maxTokens,
      String modelId,
      Float temperature,
      Integer topK,
      Float topP,
      Float frequencyPenalty,
      Float presencePenalty,
      String projectId,
      String endpointId,
      String region,
      List<String> stopSequences,
      List<String> images,
      List<String> imageProperties) implements GenerativeProvider {

    public static Provider vertex(
        String projectId,
        Function<GoogleGenerative.Provider.VertexBuilder, ObjectBuilder<GoogleGenerative.Provider>> fn) {
      return fn.apply(new VertexBuilder(projectId)).build();
    }

    public static Provider aiStudio(
        Function<GoogleGenerative.Provider.AiStudioBuilder, ObjectBuilder<GoogleGenerative.Provider>> fn) {
      return fn.apply(new AiStudioBuilder()).build();
    }

    @Override
    public void appendTo(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative.GenerativeProvider.Builder req) {
      var provider = WeaviateProtoGenerative.GenerativeGoogle.newBuilder();
      if (apiEndpoint != null) {
        provider.setApiEndpoint(apiEndpoint);
      }
      if (maxTokens != null) {
        provider.setMaxTokens(maxTokens);
      }
      if (modelId != null) {
        provider.setModel(modelId);
      }
      if (temperature != null) {
        provider.setTemperature(temperature);
      }
      if (topK != null) {
        provider.setTopK(topK);
      }
      if (topP != null) {
        provider.setTopP(topP);
      }
      if (projectId != null) {
        provider.setProjectId(projectId);
      }
      if (endpointId != null) {
        provider.setEndpointId(endpointId);
      }
      if (region != null) {
        provider.setRegion(region);
      }
      if (frequencyPenalty != null) {
        provider.setFrequencyPenalty(frequencyPenalty);
      }
      if (presencePenalty != null) {
        provider.setPresencePenalty(presencePenalty);
      }
      if (stopSequences != null) {
        provider.setStopSequences(WeaviateProtoBase.TextArray.newBuilder()
            .addAllValues(stopSequences));
      }
      req.setGoogle(provider);
    }

    public Provider(Builder builder) {
      this(
          builder.apiEndpoint,
          builder.maxTokens,
          builder.modelId,
          builder.temperature,
          builder.topK,
          builder.topP,
          builder.frequencyPenalty,
          builder.presencePenalty,
          builder.projectId,
          builder.endpointId,
          builder.region,
          builder.stopSequences,
          builder.images,
          builder.imageProperties);
    }

    public abstract static class Builder implements ObjectBuilder<GoogleGenerative.Provider> {
      private final String projectId;
      private String apiEndpoint;

      private Integer topK;
      private Float topP;
      private String modelId;
      private Integer maxTokens;
      private Float temperature;
      private Float frequencyPenalty;
      private Float presencePenalty;
      private String endpointId;
      private String region;
      private final List<String> stopSequences = new ArrayList<>();
      private final List<String> images = new ArrayList<>();
      private final List<String> imageProperties = new ArrayList<>();

      public Builder(String apiEndpoint, String projectId) {
        this.projectId = projectId;
        this.apiEndpoint = apiEndpoint;
      }

      /** Base URL of the generative provider. */
      protected Builder apiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
        return this;
      }

      public Builder topK(int topK) {
        this.topK = topK;
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
      public Builder modelId(String modelId) {
        this.modelId = modelId;
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

      public Builder endpointId(String endpointId) {
        this.endpointId = endpointId;
        return this;
      }

      public Builder region(String region) {
        this.region = region;
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
      public GoogleGenerative.Provider build() {
        return new GoogleGenerative.Provider(this);
      }
    }

    public static class AiStudioBuilder extends Builder {
      public AiStudioBuilder() {
        super(Text2VecGoogleVectorizer.AiStudioBuilder.BASE_URL, null);
      }
    }

    public static class VertexBuilder extends Builder {
      public VertexBuilder(String projectId) {
        super(Text2VecGoogleVectorizer.VertexBuilder.DEFAULT_BASE_URL, projectId);
      }

      /** Base URL of the generative provider. */
      public VertexBuilder apiEndpoint(String apiEndpoint) {
        super.apiEndpoint(apiEndpoint);
        return this;
      }
    }
  }
}
