package io.weaviate.client6.v1.api.collections.query;

import java.util.List;

public record QueryResponseGroup<T>(
    String name,
    Float minDistance,
    Float maxDistance,
    long numberOfObjects,
    List<QueryObjectGrouped<T>> objects) {
}
