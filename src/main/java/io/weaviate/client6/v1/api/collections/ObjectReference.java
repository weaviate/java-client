package io.weaviate.client6.v1.api.collections;

import java.util.List;

import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

public record ObjectReference<T>(
    List<WeaviateObject<T, ObjectReference<? extends Object>, QueryMetadata>> objects) {
}
