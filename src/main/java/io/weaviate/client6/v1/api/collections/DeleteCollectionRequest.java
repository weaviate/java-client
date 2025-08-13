package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteCollectionRequest(String collectionName) {
  public static final Endpoint<DeleteCollectionRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/schema/" + request.collectionName,
      request -> Collections.emptyMap());
}
