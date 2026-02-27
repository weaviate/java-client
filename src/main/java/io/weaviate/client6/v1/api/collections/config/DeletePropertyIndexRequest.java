package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeletePropertyIndexRequest(String collectionName, String propertyName, PropertyIndexType indexType) {
  public static final Endpoint<DeletePropertyIndexRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/schema/" + request.collectionName + "/properties/" + request.propertyName + "/index/"
          + request.indexType.toString(),
      request -> Collections.emptyMap());
}
