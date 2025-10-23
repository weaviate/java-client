package io.weaviate.client6.v1.api.collections.generate;

import java.util.List;

import io.weaviate.client6.v1.api.collections.query.QueryObjectGrouped;

public record GenerativeResponseGroup<PropertiesT>(
    /** Group name. */
    String name,
    /**
     * The smallest distance value among all objects in the group, indicating the
     * most similar object in that group to the query
     */
    Float minDistance,
    /**
     * The largest distance value among all objects in the group, indicating the
     * least similar object in that group to the query.
     */
    Float maxDistance,
    /** The size of the group. */
    long numberOfObjects,
    /** Objects retrieved in the query. */
    List<QueryObjectGrouped<PropertiesT>> objects,
    /** Output of the summary task for this group. */
    TaskOutput generated) {
}
