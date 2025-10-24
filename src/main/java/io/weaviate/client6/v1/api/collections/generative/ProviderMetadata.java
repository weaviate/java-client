package io.weaviate.client6.v1.api.collections.generative;

public interface ProviderMetadata {
  record Usage(
      Long promptTokens,
      Long completionTokens,
      Long totalTokens) {
  }
}
