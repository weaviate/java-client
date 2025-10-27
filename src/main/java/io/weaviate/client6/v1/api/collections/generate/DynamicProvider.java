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

public interface DynamicProvider {
  void appendTo(WeaviateProtoGenerative.GenerativeProvider.Builder req);

  /**
   * Configure {@code generative-anthropic} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider anthropic(
      Function<AnthropicGenerative.Provider.Builder, ObjectBuilder<AnthropicGenerative.Provider>> fn) {
    return AnthropicGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-anyscale} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider anyscale(
      Function<AnyscaleGenerative.Provider.Builder, ObjectBuilder<AnyscaleGenerative.Provider>> fn) {
    return AnyscaleGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-aws} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider aws(
      Function<AwsGenerative.Provider.Builder, ObjectBuilder<AwsGenerative.Provider>> fn) {
    return AwsGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-cohere} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider cohere(
      Function<CohereGenerative.Provider.Builder, ObjectBuilder<CohereGenerative.Provider>> fn) {
    return CohereGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-databricks} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider databricks(
      Function<DatabricksGenerative.Provider.Builder, ObjectBuilder<DatabricksGenerative.Provider>> fn) {
    return DatabricksGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-friendliai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider friendliai(
      Function<FriendliaiGenerative.Provider.Builder, ObjectBuilder<FriendliaiGenerative.Provider>> fn) {
    return FriendliaiGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-palm} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider google(
      Function<GoogleGenerative.Provider.Builder, ObjectBuilder<GoogleGenerative.Provider>> fn) {
    return GoogleGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-mistral} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider mistral(
      Function<MistralGenerative.Provider.Builder, ObjectBuilder<MistralGenerative.Provider>> fn) {
    return MistralGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-nvidia} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider nvidia(
      Function<NvidiaGenerative.Provider.Builder, ObjectBuilder<NvidiaGenerative.Provider>> fn) {
    return NvidiaGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-ollama} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider ollama(
      Function<OllamaGenerative.Provider.Builder, ObjectBuilder<OllamaGenerative.Provider>> fn) {
    return OllamaGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-openai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider openai(
      Function<OpenAiGenerative.Provider.Builder, ObjectBuilder<OpenAiGenerative.Provider>> fn) {
    return OpenAiGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-openai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider azure(
      Function<AzureOpenAiGenerative.Provider.Builder, ObjectBuilder<AzureOpenAiGenerative.Provider>> fn) {
    return AzureOpenAiGenerative.Provider.of(fn);
  }

  /**
   * Configure {@code generative-xai} as a dynamic provider.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static DynamicProvider xai(
      Function<XaiGenerative.Provider.Builder, ObjectBuilder<XaiGenerative.Provider>> fn) {
    return XaiGenerative.Provider.of(fn);
  }

}
