package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record GetConfigRequest(String collectionName) {
  public static <T> Endpoint<GetConfigRequest, Optional<T>> endpoint(Class<T> cls) {
    return OptionalEndpoint.noBodyOptional(
        request -> "GET",
        request -> "/schema/" + request.collectionName,
        request -> Collections.emptyMap(),
        cls);
  }
}
