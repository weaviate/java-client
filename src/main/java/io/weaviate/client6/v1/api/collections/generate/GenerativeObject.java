package io.weaviate.client6.v1.api.collections.generate;

import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

public record GenerativeObject<PropertiesT>(
    /** Object properties. */
    PropertiesT properties,
    /** Object metadata. */
    QueryMetadata metadata,
    /** Generative task output. */
    TaskOutput generated) {
}
