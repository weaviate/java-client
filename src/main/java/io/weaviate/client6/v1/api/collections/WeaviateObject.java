package io.weaviate.client6.v1.api.collections;

import java.util.Map;

public record WeaviateObject<T>(
    String collection,
    T properties,
    Map<String, ObjectReference> references,
    ObjectMetadata metadata) {
}
