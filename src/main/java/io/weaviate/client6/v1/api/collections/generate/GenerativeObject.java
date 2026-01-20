package io.weaviate.client6.v1.api.collections.generate;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

public record GenerativeObject<PropertiesT>(
    /** Object UUID. */
    String uuid,
    /** Retrieved object vectors. */
    Vectors vectors,
    /** Object properties. */
    PropertiesT properties,
    /** Object metadata. */
    QueryMetadata metadata,
    /** Generative task output. */
    TaskOutput generative) {
}
