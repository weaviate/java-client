package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteObjectRequest(String collectionName, String uuid) {

  public static final Endpoint<DeleteObjectRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/objects/" + request.collectionName + "/" + request.uuid,
      request -> Collections.emptyMap());
}
