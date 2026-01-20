package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record GetConfigRequest(String collectionName) {
  public static final Endpoint<GetConfigRequest, Optional<CollectionConfig>> _ENDPOINT = OptionalEndpoint
      .noBodyOptional(
          request -> "GET",
          request -> "/schema/" + request.collectionName,
          request -> Collections.emptyMap(),
          (statusCode, response) -> JSON.deserialize(response, CollectionConfig.class));
}
