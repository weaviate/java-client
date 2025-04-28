package io.weaviate.client6.v1.collections.object;

import java.util.List;

public record ObjectReference(List<WeaviateObject<?>> objects) {
}
