package io.weaviate.client6.v1.api.collections.generative;

import io.weaviate.client6.v1.api.collections.Generative;

public interface ProviderMetadata {
  Generative.Kind _kind();

  record Usage(
      Long promptTokens,
      Long completionTokens,
      Long totalTokens) {
  }
}
