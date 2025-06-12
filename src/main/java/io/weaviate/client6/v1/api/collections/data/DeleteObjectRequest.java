package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.internal.rest.Endpoint;

public record DeleteObjectRequest(String collectionName, String uuid) {

  public static final Endpoint<DeleteObjectRequest, Void> _ENDPOINT = Endpoint.of(
      request -> "DELETE",
      request -> "/objects/" + request.collectionName + "/" + request.uuid,
      (gson, request) -> null,
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_NO_CONTENT,
      (gson, response) -> null);
}
