package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteVectorIndexRequest(String collectionName, String vectorName) {
  public static final Endpoint<DeleteVectorIndexRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/schema/" + request.collectionName + "/vectors/" + request.vectorName + "/index",
      request -> Collections.emptyMap());
}
