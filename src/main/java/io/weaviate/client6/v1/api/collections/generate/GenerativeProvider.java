package io.weaviate.client6.v1.api.collections.generate;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.generative.AnthropicGenerative;
import io.weaviate.client6.v1.api.collections.generative.AnyscaleGenerative;
import io.weaviate.client6.v1.api.collections.generative.AwsGenerative;
import io.weaviate.client6.v1.api.collections.generative.AzureOpenAiGenerative;
import io.weaviate.client6.v1.api.collections.generative.CohereGenerative;
import io.weaviate.client6.v1.api.collections.generative.DatabricksGenerative;
import io.weaviate.client6.v1.api.collections.generative.FriendliaiGenerative;
import io.weaviate.client6.v1.api.collections.generative.GoogleGenerative;
import io.weaviate.client6.v1.api.collections.generative.MistralGenerative;
import io.weaviate.client6.v1.api.collections.generative.NvidiaGenerative;
import io.weaviate.client6.v1.api.collections.generative.OllamaGenerative;
import io.weaviate.client6.v1.api.collections.generative.OpenAiGenerative;
import io.weaviate.client6.v1.api.collections.generative.XaiGenerative;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;

public interface GenerativeProvider {
  void appendTo(WeaviateProtoGenerative.GenerativeProvider.Builder req);

  /**
   * Configure {@code generative-anthropic} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider anthropic(
      Function<AnthropicGenerative.Provider.Builder, ObjectBuilder<AnthropicGenerative.Provider>> fn) {
    return AnthropicGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-anyscale} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider anyscale(
      Function<AnyscaleGenerative.Provider.Builder, ObjectBuilder<AnyscaleGenerative.Provider>> fn) {
    return AnyscaleGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-aws} as a dynamic provider.
   *
   * @param region AWS region.
   * @param model  Inference model.
   * @param fn     Lambda expression for optional parameters.
   */
  public static GenerativeProvider awsBedrock(
      String region,
      String model,
      Function<AwsGenerative.Provider.BedrockBuilder, ObjectBuilder<AwsGenerative.Provider>> fn) {
    return AwsGenerative.Provider.bedrock(region, model, fn);
  }

  /**
   * Configure {@code generative-aws} as a dynamic provider.
   *
   * @param region   AWS region.
   * @param endpoint Base inference URL.
   * @param fn       Lambda expression for optional parameters.
   */
  public static GenerativeProvider awsSagemaker(
      String region,
      String endpoint,
      Function<AwsGenerative.Provider.SagemakerBuilder, ObjectBuilder<AwsGenerative.Provider>> fn) {
    return AwsGenerative.Provider.sagemaker(region, endpoint, fn);
  }

  /**
   * Configure {@code generative-cohere} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider cohere(
      Function<CohereGenerative.Provider.Builder, ObjectBuilder<CohereGenerative.Provider>> fn) {
    return CohereGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-databricks} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider databricks(
      Function<DatabricksGenerative.Provider.Builder, ObjectBuilder<DatabricksGenerative.Provider>> fn) {
    return DatabricksGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-friendliai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider friendliai(
      Function<FriendliaiGenerative.Provider.Builder, ObjectBuilder<FriendliaiGenerative.Provider>> fn) {
    return FriendliaiGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-palm} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider googleAiStudio(
      Function<GoogleGenerative.Provider.AiStudioBuilder, ObjectBuilder<GoogleGenerative.Provider>> fn) {
    return GoogleGenerative.Provider.aiStudio(fn);
  }

  /**
   * Configure {@code generative-palm} as a dynamic provider.
   *
   * @param projectId Google project ID.
   * @param fn        Lambda expression for optional parameters.
   */
  public static GenerativeProvider googleVertex(
      String projectId,
      Function<GoogleGenerative.Provider.VertexBuilder, ObjectBuilder<GoogleGenerative.Provider>> fn) {
    return GoogleGenerative.Provider.vertex(projectId, fn);
  }

  /**
   * Configure {@code generative-mistral} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider mistral(
      Function<MistralGenerative.Provider.Builder, ObjectBuilder<MistralGenerative.Provider>> fn) {
    return MistralGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-nvidia} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider nvidia(
      Function<NvidiaGenerative.Provider.Builder, ObjectBuilder<NvidiaGenerative.Provider>> fn) {
    return NvidiaGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-ollama} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider ollama(
      Function<OllamaGenerative.Provider.Builder, ObjectBuilder<OllamaGenerative.Provider>> fn) {
    return OllamaGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-openai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider openai(
      Function<OpenAiGenerative.Provider.Builder, ObjectBuilder<OpenAiGenerative.Provider>> fn) {
    return OpenAiGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-openai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider azure(
      Function<AzureOpenAiGenerative.Provider.Builder, ObjectBuilder<AzureOpenAiGenerative.Provider>> fn) {
    return AzureOpenAiGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-xai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static GenerativeProvider xai(
      Function<XaiGenerative.Provider.Builder, ObjectBuilder<XaiGenerative.Provider>> fn) {
    return XaiGenerative.Provider.of(fn);
  }

}
