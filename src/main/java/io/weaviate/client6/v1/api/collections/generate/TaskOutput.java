package io.weaviate.client6.v1.api.collections.generate;

import io.weaviate.client6.v1.api.collections.generative.ProviderMetadata;

public record TaskOutput(
    String text,
    ProviderMetadata metadata,
    GenerativeDebug debug) {

}
