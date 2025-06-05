package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record GetConfigRequest(String collectionName) {
  public static final Endpoint<GetConfigRequest, Optional<WeaviateCollection>> _ENDPOINT = Endpoint.of(
      request -> "GET",
      request -> "/schema/" + request.collectionName,
      (gson, request) -> null,
      request -> Collections.emptyMap(),
      code -> code != 200,
      (gson, response) -> Optional.ofNullable(JSON.deserialize(response, WeaviateCollection.class)));
}
