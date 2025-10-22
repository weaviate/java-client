package io.weaviate.client6.v1.api.collections.generate;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.generative.AnthropicGenerative;
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
}
