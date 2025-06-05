package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record AddPropertyRequest(String collectionName, Property property) {
  public static final Endpoint<AddPropertyRequest, Void> _ENDPOINT = Endpoint.of(
      request -> "POST",
      request -> "/schema/" + request.collectionName + "/properties",
      (gson, request) -> JSON.serialize(request.property),
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_SUCCESS,
      (gson, response) -> null);
}
