package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListCollectionRequest() {
  public static <T> Endpoint<ListCollectionRequest, T> endpoint(Class<T> cls) {
    return SimpleEndpoint.noBody(
        request -> "GET",
        request -> "/schema",
        request -> Collections.emptyMap(),
        cls);
  }
}
