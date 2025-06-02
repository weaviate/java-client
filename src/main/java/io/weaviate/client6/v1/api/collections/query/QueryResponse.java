package io.weaviate.client6.v1.api.collections.query;

import java.util.List;

import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record QueryResponse<T>(List<WeaviateObject<T, QueryMetadata>> objects) {
}
