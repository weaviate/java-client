package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;

import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record AddPropertyRequest(String collectionName, Property property) {
  public static final Endpoint<AddPropertyRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "POST",
      request -> "/schema/" + request.collectionName + "/properties",
      request -> Collections.emptyMap(),
      request -> JSON.serialize(request.property));
}
