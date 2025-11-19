package io.weaviate.client6.v1.api.collections.generate;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

public record GenerativeObject<PropertiesT>(
        /** Object properties. */
        PropertiesT properties,
        /** Object metadata. */
        QueryMetadata metadata,
        /** Generative task output. */
        TaskOutput generative) {

    /** Shorthand for accessing objects's UUID from metadata. */
    public String uuid() {
        return metadata.uuid();
    }

    /** Shorthand for accessing objects's vectors from metadata. */
    public Vectors vectors() {
        return metadata.vectors();
    }
}
