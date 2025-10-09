package io.weaviate.client6.v1.api.collections.generate;

public record TaskOutput(
    String text,
    ProviderMetadata metadata,
    GenerativeDebug debug) {

}
