package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.internal.rest.Endpoint;

public record DeleteCollectionRequest(String collectionName) {
  public static final Endpoint<DeleteCollectionRequest, Void> _ENDPOINT = Endpoint.of(
      request -> "DELETE",
      request -> "/schema/" + request.collectionName,
      (gson, request) -> null,
      request -> Collections.emptyMap(),
      status -> status != HttpStatus.SC_SUCCESS,
      (gson, resopnse) -> null);
}
