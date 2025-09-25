package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Map;

public record QueryResponseGrouped<T>(
    /** All objects retrieved in the query. */
    List<QueryObjectGrouped<T>> objects,
    /** Grouped response objects. */
    Map<String, QueryResponseGroup<T>> groups) {
}
