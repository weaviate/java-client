package io.weaviate.client6.v1.api.collections;

import java.util.List;

public record ObjectReference(List<WeaviateObject<?>> objects) {
}
