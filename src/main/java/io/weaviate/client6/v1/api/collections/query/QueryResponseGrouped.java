package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Map;

public record QueryResponseGrouped<T>(
    List<QueryObjectGrouped<T>> objects,
    Map<String, QueryResponseGroup<T>> groups) {
}
