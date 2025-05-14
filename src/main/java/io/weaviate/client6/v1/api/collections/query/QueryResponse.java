package io.weaviate.client6.v1.api.collections.query;

import java.util.List;

public record QueryResponse<T>(List<QueryObject<T>> objects) {
}
